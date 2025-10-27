package com.example.cmuapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cmuapp.data.entities.UserEntity

/**
 * Data Access Object for the UserEntity.
 * Provides methods to interact with the user_table in the Room database.
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}
