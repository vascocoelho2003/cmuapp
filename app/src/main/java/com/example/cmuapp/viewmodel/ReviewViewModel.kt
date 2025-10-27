package com.example.cmuapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.data.repository.EstablishmentRepository
import com.example.cmuapp.data.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import distanceInMeters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository,
    private val firestore: FirebaseFirestore,
    private val establishmentRepository: EstablishmentRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> get() = _reviews

    private val _canSubmit = MutableLiveData<Boolean>()
    val canSubmit: LiveData<Boolean> get() = _canSubmit

    /**
     * Loads reviews for a specific establishment (keeps behaviour igual).
     * Se quiseres, podemos também fazer fallback para Room aqui (se já tiveres DAO com essa query).
     */
    fun loadReviews(establishmentId: String) {
        viewModelScope.launch {
            val data = repository.getReviewsFromFirestore(establishmentId)
            _reviews.postValue(data)
        }
    }

    /**
     * Retrieves the username for a given user ID.
     */
    suspend fun getUserName(userId: String): String {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            snapshot.getString("username") ?: "Utilizador desconhecido"
        } catch (e: Exception) {
            "Utilizador desconhecido"
        }
    }

    /**
     * Submits a new review (já lida com offline dentro do repository).
     */
    fun submitReview(review: Review) {
        viewModelScope.launch {
            repository.addReview(review)
        }
    }

    /**
     * Refreshes user reviews by fetching them from Firestore only if online.
     * Passar o contexto para decidir online/offline.
     */
    fun refreshUserReviews(userId: String, context: Context) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val online = com.example.cmuapp.utils.isOnline(context)
                if (online) {
                    repository.fetchUserReviewsFromFirestore(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Returns a Flow of user reviews.
     * Internamente o repository decide: se online sincroniza remoto -> Room e depois emite o Flow do Room;
     * se offline emite directamente o Flow do Room.
     *
     * Nota: passamos o context aqui para decidir online/offline.
     */
    fun getUserReviews(userId: String, context: Context): Flow<List<Review>> {
        val online = com.example.cmuapp.utils.isOnline(context)
        return repository.getUserReviewsFlow(userId, online)
    }

    /**
     * Retrieves the name of an establishment by its ID.
     * Tenta primeiro a cache local (Room) e só depois Firestore.
     */
    suspend fun getEstablishmentName(establishmentId: String): String {
        return try {
            val local = establishmentRepository.getEstablishmentById(establishmentId)
            if (local != null) {
                local.name ?: "Estabelecimento desconhecido"
            } else {
                val snapshot = firestore.collection("establishments")
                    .document(establishmentId)
                    .get()
                    .await()
                snapshot.getString("name") ?: "Estabelecimento desconhecido"
            }
        } catch (e: Exception) {
            "Estabelecimento desconhecido"
        }
    }

    /**
     * Checks if a user can submit a review based on proximity and time since last review.
     * Usa repository.getLastUserReview(...) que já faz fallback local/remote (se implementaste).
     */
    fun checkIfUserCanSubmit(estId: String, userId: String, userLat: Double, userLon: Double) {
        viewModelScope.launch {
            val est = establishmentRepository.getEstablishmentFromFirestoreById(estId) ?: establishmentRepository.getEstablishmentById(estId)
            if (est != null) {
                val dist = distanceInMeters(userLat, userLon, est.lat ?: 0.0, est.lon ?: 0.0)
                if (dist <= 50f) {
                    val lastReview = repository.getLastUserReview(userId, estId)
                    val now = System.currentTimeMillis()
                    _canSubmit.postValue(lastReview == null || now - lastReview.timestamp > 30 * 60 * 1000)
                } else {
                    _canSubmit.postValue(false)
                }
            } else {
                _canSubmit.postValue(false)
            }
        }
    }
}
