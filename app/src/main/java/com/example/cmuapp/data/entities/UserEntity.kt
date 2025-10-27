package com.example.cmuapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a user entity in the database.
 */
@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val username: String
)
