package com.example.cmuapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.example.cmuapp.data.database.AppDatabase
import com.example.cmuapp.data.entities.UserEntity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to manage user data, including fetching from Firestore and caching in Room database.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> = _userData

    /**
     * Fetch user data from Firestore and cache it in Room database.
     */
    fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val cachedUser = db.userDao().getUser()
            if (cachedUser != null) {
                _userData.value = cachedUser
            }
        }

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val email = doc.getString("email") ?: ""
                    val username = doc.getString("username") ?: ""
                    val user = UserEntity(uid, email, username)

                    _userData.value = user

                    viewModelScope.launch {
                        db.userDao().insertUser(user)
                    }
                }
            }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid
}
