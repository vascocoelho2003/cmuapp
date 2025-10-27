package com.example.cmuapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cmuapp.data.entities.Review
import kotlinx.coroutines.flow.Flow

/**
 *  Data Access Object (DAO) for the Review entity.
 *  Provides methods for interacting with the reviews table in the Room database.
 */
@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<Review>)

    @Query("SELECT * FROM reviews WHERE establishmentId = :establishmentId")
    fun getReviewsByEstablishment(establishmentId: String): Flow<List<Review>>

    @Query("SELECT * FROM reviews")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE synced = 0")
    suspend fun getUnsyncedReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserReviewsFlow(userId: String): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserReviewsOnce(userId: String): List<Review>

    @Update
    suspend fun updateReview(review: Review)

    @Query("DELETE FROM reviews WHERE userId = :userId")
    suspend fun deleteReviewsByUser(userId: String)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()

    @Query("SELECT * FROM reviews WHERE userId = :userId AND establishmentId = :estId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUserReview(userId: String, estId: String): Review?

}