package com.example.cmuapp.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Data class representing an establishment entity in the database.
 * Implements Parcelable to allow passing instances between Android components.
 */
@Parcelize
@Entity(tableName = "establishment")
data class Establishment(
    @PrimaryKey var id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String? = null,
    val rating: Float? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val avgRating: Double = 0.0,
    val totalReviews: Int = 0,
    val imageUrl: String? = null
) : Parcelable
