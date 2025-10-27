package com.example.cmuapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmuapp.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to manage user authentication using Firebase Authentication and Firestore.
 * It provides methods for login, signup, signout, and checking authentication status.
 * The authentication state is exposed via LiveData to allow UI components to observe changes.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val reviewRepository: ReviewRepository) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    val currentUserId : String?
        get() = auth.currentUser?.uid

    /**
     * Check the current authentication status of the user and update the authState LiveData accordingly.
     */
    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    /**
     * Attempt to log in a user with the provided email and password.
     */
    fun login(email: String, password: String) {

        if(email.isEmpty() || password.isEmpty()){
            _authState.value= AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value= AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value= AuthState.Authenticated
                }else{
                    _authState.value= AuthState.Error(task.exception?.message?:"Something went wrong")
                }
        }
    }

    /**
     * Attempt to sign up a new user with the provided email, password, and username.
     */
    fun signup(email: String, password: String, username: String) {

        if(email.isEmpty() || password.isEmpty() || username.isEmpty()){
            _authState.value= AuthState.Error("Email, password or username can't be empty")
            return
        }

        _authState.value= AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userMap = hashMapOf(
                        "email" to email,
                        "username" to username,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            _authState.value = AuthState.Authenticated
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error(e.message ?: "Error saving user")
                        }

                }else{
                    _authState.value= AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    /**
     * Sign out the current user and clear local data.
     */
    fun signout(){
        viewModelScope.launch {
            reviewRepository.clearLocalData()
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}