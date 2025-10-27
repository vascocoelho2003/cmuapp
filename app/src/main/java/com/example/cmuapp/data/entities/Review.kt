package com.example.cmuapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a review entity in the database.
 */
@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String = "",
    var establishmentId: String="",
    val userId: String="",
    val rating: Int=0,
    val docaria : String = "",
    val comment: String="",
    val audioUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val synced: Boolean = false
)
