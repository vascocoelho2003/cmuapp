package com.example.cmuapp.data.API

import com.example.cmuapp.data.entities.Place
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Google Places API interface for fetching nearby places.
 */
data class PlacesResponse(
    val results: List<Place>
)

interface GooglePlacesApi {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String?,
        @Query("key") apiKey: String
    ): PlacesResponse

    companion object {
        fun create(retrofit: Retrofit): GooglePlacesApi {
            return retrofit.create(GooglePlacesApi::class.java)
        }
    }
}