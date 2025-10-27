package com.example.cmuapp.data.repository

import android.content.Context
import com.example.cmuapp.data.dao.EstablishmentDao
import com.example.cmuapp.data.dao.ReviewDao
import com.example.cmuapp.data.entities.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class RankingRepository(
    private val establishmentDao: EstablishmentDao,
    private val firestore: FirebaseFirestore,
    private val reviewDao: ReviewDao
) {

    /**
     * Gets the top 10 docarias based on average ratings from reviews.
     */
    suspend fun getTopDocarias(context: Context): List<DocariaStats> {
        val online = com.example.cmuapp.utils.isOnline(context)

        val establishments = establishmentDao.getAllEstablishments().first()
        val globalDocarias = mutableListOf<DocariaStats>()

        for (est in establishments) {
            val reviews: List<Review> = try {
                if (online) {
                    val reviewsSnapshot = firestore.collection("establishments")
                        .document(est.id)
                        .collection("reviews")
                        .get()
                        .await()
                    reviewsSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Review::class.java)?.copy(id = doc.id)
                    }
                } else {
                    reviewDao.getReviewsByEstablishment(est.id).first()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            for (review in reviews) {
                val name = review.docaria.takeIf { it.isNotBlank() } ?: continue
                val existing = globalDocarias.find { it.name == name && it.establishmentId == est.id }
                if (existing != null) {
                    existing.totalRating += review.rating
                    existing.count += 1
                } else {
                    globalDocarias.add(
                        DocariaStats(
                            name = name,
                            totalRating = review.rating,
                            count = 1,
                            establishmentId = est.id,
                            establishmentName = est.name
                        )
                    )
                }
            }
        }

        return globalDocarias
            .sortedByDescending { it.avg }
            .take(10)
    }
}


data class DocariaStats(
    val name: String,
    var totalRating: Int = 0,
    var count: Int = 0,
    var establishmentId: String? = null,
    var establishmentName: String? = null
) {
    val avg: Float
        get() = if (count > 0) totalRating.toFloat() / count else 0f
}

