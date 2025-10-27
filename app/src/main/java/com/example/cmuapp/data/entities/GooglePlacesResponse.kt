package com.example.cmuapp.data.entities

/**
 * Data classes representing the structure of the Google Places API response.
 */
data class Place(
    val place_id: String,
    val name: String,
    val geometry: Geometry,
    val vicinity: String,
    val rating: Double? = null,
    val user_ratings_total: Int? = null,
    val photos: List<Photo>? = null
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Photo(
    val photo_reference: String,
    val height: Int,
    val width: Int
)