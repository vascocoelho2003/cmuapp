package com.example.cmuapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cmuapp.data.entities.UserVisitedEntity

@Dao
interface UserVisitedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<UserVisitedEntity>)

    @Query("SELECT establishmentId FROM user_visited WHERE userId = :userId")
    suspend fun getEstablishmentIdsForUser(userId: String): List<String>
}
