package com.example.cmuapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.cmuapp.data.dao.EstablishmentDao
import com.example.cmuapp.data.dao.ReviewDao
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.utils.isOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing reviews.
 */
@Singleton
class ReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val appContext: Context,
) {

    /**
     * Gets reviews for a specific establishment.
     */
    fun getReviewsByEstablishment(establishmentId: String) =
        reviewDao.getReviewsByEstablishment(establishmentId)

    /**
     * Gets all reviews from the local database.
     */
    fun getAllReviews() = reviewDao.getAllReviews()

    /**
     * Saves a review locally in the Room database.
     */
    suspend fun saveReviewLocal(review: Review): Long {
        return reviewDao.insertReview(review)
    }

    /**
     * Adds a review, uploading media if necessary and syncing with Firestore.
     */
    suspend fun addReview(review: Review) {
        withContext(Dispatchers.IO) {
            try {
                reviewDao.insertReview(review.copy(synced = false))

                if (!isOnline(appContext)) {
                    println("📴 Offline: review salva apenas no Room")
                    return@withContext
                }

                val docId = if (review.id.isBlank()) UUID.randomUUID().toString() else review.id

                val reviewWithId = review.copy(id = docId, synced = false)

                val (finalImageUrl, finalAudioUrl) = uploadMediaIfNeededAndSend(reviewWithId, docId)

                val syncedReview = reviewWithId.copy(
                    imageUrl = finalImageUrl,
                    audioUrl = finalAudioUrl,
                    synced = true
                )
                reviewDao.insertReview(syncedReview)

            } catch (e: Exception) {
                e.printStackTrace()
                reviewDao.insertReview(review.copy(synced = false))
            }
        }
    }

    /**
     * Syncs any pending reviews that haven't been synced yet.
     */
    suspend fun syncPendingReviews() {
        withContext(Dispatchers.IO) {
            val unsynced = reviewDao.getUnsyncedReviews()
            for (review in unsynced) {
                try {
                    val docId = if (review.id.isBlank()) UUID.randomUUID().toString() else review.id
                    val reviewWithId = review.copy(id = docId, synced = false)

                    val (finalImageUrl, finalAudioUrl) = uploadMediaIfNeededAndSend(reviewWithId, docId)

                    val syncedReview = reviewWithId.copy(
                        imageUrl = finalImageUrl,
                        audioUrl = finalAudioUrl,
                        synced = true
                    )
                    reviewDao.insertReview(syncedReview)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Uploads media files if needed and sends the review to Firestore.
     */
    private suspend fun uploadMediaIfNeededAndSend(review: Review, docId: String): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@withContext Pair(review.imageUrl, review.audioUrl)
            val userId = if (review.userId.isBlank()) currentUser.uid else review.userId

            var finalImageUrl: String? = review.imageUrl
            var finalAudioUrl: String? = review.audioUrl

            finalImageUrl?.let { path ->
                try {
                    val imageUri = when {
                        path.startsWith("content://") -> Uri.parse(path)
                        path.startsWith("file://") -> Uri.parse(path)
                        else -> {
                            val f = File(path)
                            if (f.exists()) Uri.fromFile(f) else null
                        }
                    }
                    if (imageUri != null) {
                        finalImageUrl = uploadImageToStorage(review.copy(userId = userId), imageUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    finalImageUrl = null
                }
            }

            finalAudioUrl?.let { path ->
                try {
                    val audioUri = when {
                        path.startsWith("content://") -> Uri.parse(path)
                        path.startsWith("file://") -> Uri.parse(path)
                        else -> {
                            val f = File(path)
                            if (f.exists()) Uri.fromFile(f) else null
                        }
                    }
                    if (audioUri != null) {
                        finalAudioUrl = uploadAudioToStorage(review.copy(userId = userId), audioUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    finalAudioUrl = null
                }
            }

            val reviewData = hashMapOf(
                "id" to docId,
                "establishmentId" to review.establishmentId,
                "userId" to userId,
                "rating" to review.rating,
                "comment" to review.comment,
                "docaria" to review.docaria,
                "timestamp" to review.timestamp,
                "imageUrl" to finalImageUrl,
                "audioUrl" to finalAudioUrl
            )

            val estDocRef = firestore.collection("establishments").document(review.establishmentId)
            val reviewDocRef = estDocRef.collection("reviews").document(docId)

            firestore.runTransaction { transaction ->
                val estSnap = transaction.get(estDocRef)
                val currentTotal = estSnap.getLong("totalReviews") ?: 0L
                val currentAvg = estSnap.getDouble("avgRating") ?: 0.0

                val ratingDouble = review.rating.toDouble()
                val newTotal = currentTotal + 1L
                val newAvg = if (newTotal > 0L) {
                    ((currentAvg * currentTotal) + ratingDouble) / newTotal
                } else {
                    ratingDouble
                }

                transaction.update(estDocRef, mapOf(
                    "avgRating" to newAvg,
                    "totalReviews" to newTotal
                ))

                transaction.set(reviewDocRef, reviewData)
            }.await()

            Pair(finalImageUrl, finalAudioUrl)
        }
    }

    /**
     * Uploads an image to Firebase Storage and returns its download URL.
     */
    private suspend fun uploadImageToStorage(review: Review, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val filename = "reviews/${review.userId}/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(filename)

            val inputStream: InputStream? = tryOpenInputStreamForUri(uri)
                ?: throw IllegalArgumentException("Não foi possível abrir input stream para URI: $uri")

            inputStream.use { stream ->
                if (stream != null) {
                    storageRef.putStream(stream).await()
                }
            }

            storageRef.downloadUrl.await().toString()
        }
    }

    /**
     * Uploads an audio file to Firebase Storage and returns its download URL.
     */
    private suspend fun uploadAudioToStorage(review: Review, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val filename = "reviews/${review.userId}/${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference.child(filename)

            val inputStream: InputStream? = tryOpenInputStreamForUri(uri)
                ?: throw IllegalArgumentException("Não foi possível abrir input stream para URI: $uri")

            inputStream.use { stream ->
                if (stream != null) {
                    storageRef.putStream(stream).await()
                }
            }

            storageRef.downloadUrl.await().toString()
        }
    }

    /**
     * Tries to open an InputStream for a given URI, handling different URI schemes.
     */
    private fun tryOpenInputStreamForUri(uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                if (file.exists()) return file.inputStream()
            } else {
                val maybePath = uri.path
                if (!maybePath.isNullOrBlank()) {
                    val f = File(maybePath)
                    if (f.exists()) return f.inputStream()
                }
            }
            appContext.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fetches reviews for a specific establishment from Firestore.
     */
    suspend fun getReviewsFromFirestore(establishmentId: String): List<Review> {
        val snapshot = firestore.collection("establishments")
            .document(establishmentId)
            .collection("reviews")
            .get()
            .await()
        return snapshot.toObjects(Review::class.java)
    }

    /**
     * Fetches reviews made by a specific user from Firestore and updates the local database.
     */
    suspend fun fetchUserReviewsFromFirestore(userId: String) {
        try {
            reviewDao.deleteReviewsByUser(userId)

            val snapshot = firestore.collectionGroup("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    val review = doc.toObject(Review::class.java)
                    val estId = doc.reference.parent.parent?.id
                    if (review != null) {
                        if (review.establishmentId.isNullOrBlank() && !estId.isNullOrBlank()) {
                            review.establishmentId = estId
                        }
                        review
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (reviews.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    reviewDao.insertAll(reviews)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Gets reviews made by a specific user from the local database.
     */
    fun getUserReviews(userId: String): Flow<List<Review>> {
        val online = isOnline(appContext)
        return getUserReviewsFlow(userId, online)
    }

    /**
     * Clears all local review data from the Room database.
     */
    suspend fun clearLocalData() {
        reviewDao.deleteAllReviews()
    }

    /**
     * Gets the most recent review made by a user for a specific establishment.
     */
    suspend fun getLastUserReview(userId: String, establishmentId: String): Review? {
        return try {
            if (isOnline(appContext)) {
                val snapshot = firestore.collection("establishments")
                    .document(establishmentId)
                    .collection("reviews")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                snapshot.toObjects(Review::class.java).firstOrNull()
                    ?: reviewDao.getLastUserReview(userId, establishmentId)
            } else {
                reviewDao.getLastUserReview(userId, establishmentId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                reviewDao.getLastUserReview(userId, establishmentId)
            } catch (inner: Exception) {
                inner.printStackTrace()
                null
            }
        }
    }


    fun getUserReviewsFlow(userId: String, online: Boolean): Flow<List<Review>> = flow {
        try {
            if (online) {
                fetchUserReviewsFromFirestore(userId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        emitAll(reviewDao.getUserReviewsFlow(userId))
    }

}
