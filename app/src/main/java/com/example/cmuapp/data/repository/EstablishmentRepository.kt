package com.example.cmuapp.data.repository

import com.example.cmuapp.BuildConfig.GOOGLE_PLACES_API_KEY
import com.example.cmuapp.data.API.GooglePlacesApi
import com.example.cmuapp.data.dao.EstablishmentDao
import com.example.cmuapp.data.entities.Establishment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log
import com.example.cmuapp.data.dao.UserVisitedDao
import com.example.cmuapp.data.entities.UserVisitedEntity

/**
 * Repository class for managing Establishment data from various sources.
 */
class EstablishmentRepository @Inject constructor(
    private val establishmentDao: EstablishmentDao,
    private val api: GooglePlacesApi,
    private val firestore: FirebaseFirestore,
    private val userVisitedDao: UserVisitedDao
) {

    /**
     * Fetches nearby establishments from the Google Places API based on the provided location.
     *
     * @param location The location in "latitude,longitude" format.
     * @return A list of Establishment objects.
     */
    suspend fun fetchEstablishment(location: String): List<Establishment> {
        val radius = 1000
        val response = api.getNearbyPlaces(
            location = location,
            radius = radius,
            type = "cafe",
            apiKey = GOOGLE_PLACES_API_KEY
        )

        return response.results.map { place ->
            val photoReference = place.photos?.firstOrNull()?.photo_reference
            val imageUrl = photoReference?.let {
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$it&key=$GOOGLE_PLACES_API_KEY"
            }

            Establishment(
                id = place.place_id,
                name = place.name,
                address = place.vicinity,
                city = null,
                rating = place.rating?.toFloat() ?: 0f,
                avgRating = 0.0,
                lat = place.geometry.location.lat,
                lon = place.geometry.location.lng,
                imageUrl = imageUrl
            )
        }
    }

    /**
     * Retrieves an establishment by its ID from the local Room database.
     *
     * @param establishmentId The ID of the establishment.
     * @return The Establishment object if found, null otherwise.
     */
    suspend fun getEstablishmentById(establishmentId: String): Establishment? {
        return establishmentDao.getEstablishmentById(establishmentId)
    }

    /**
     * Saves or merges an establishment to Firestore.
     *
     * @param establishment The Establishment object to be saved.
     */
    suspend fun saveEstablishmentToFirestore(establishment: Establishment) {
        try {
            val estMap = hashMapOf(
                "id" to establishment.id,
                "name" to establishment.name,
                "address" to establishment.address,
                "city" to establishment.city,
                "rating" to establishment.rating,
                "lat" to establishment.lat,
                "lon" to establishment.lon,
                "imageUrl" to establishment.imageUrl
            )

            firestore.collection("establishments")
                .document(establishment.id)
                .set(estMap, SetOptions.merge())
                .await()

            println("Saved (merged) establishment to Firestore: ${establishment.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves all establishments from the local Room database.
     *
     * @return A list of Establishment objects.
     */
    suspend fun getAllFromRoom(): List<Establishment> {
        return establishmentDao.getAllEstablishmentsOnce()
    }

    /**
     * Saves a list of establishments to the local Room database.
     *
     * @param ests The list of Establishment objects to be saved.
     */
    suspend fun saveEstablishmentsToRoom(ests: List<Establishment>) {
        establishmentDao.insertAll(ests)
    }

    /**
     * Retrieves a leaderboard of nearby establishments based on the specified rating type.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param ratingType The type of rating to sort by ("google" or "local").
     * @param online Whether to fetch data online or use local data.
     * @return A list of top 10 Establishment objects sorted by the specified rating type.
     */
    suspend fun getNearbyLeaderboard(
        lat: Double,
        lon: Double,
        ratingType: String,
        online: Boolean
    ): List<Establishment> {
        val nearby = if (online) fetchEstablishment("$lat,$lon") else getAllFromRoom()
        val updated = mutableListOf<Establishment>()
        for (est in nearby) {
            if (online) {
                getEstablishmentFromFirestoreById(est.id)?.let {
                    establishmentDao.insert(it)
                }
            }
            updated.add(establishmentDao.getEstablishmentById(est.id) ?: est.copy(avgRating = 0.0))
        }
        return when (ratingType) {
            "google" -> updated.sortedByDescending { it.rating }.take(10)
            "local" -> updated.sortedByDescending { it.avgRating }.take(10)
            else -> updated
        }
    }


    /**
     * Synchronizes an establishment from Firestore to the local Room database.
     *
     * @param establishmentId The ID of the establishment to be synchronized.
     */
    suspend fun syncEstablishmentFromFirestore(establishmentId: String) {
        try {
            val snapshot =
                firestore.collection("establishments").document(establishmentId).get().await()
            val est = snapshot.toObject(Establishment::class.java)
            if (est != null) {
                est.id = snapshot.id
                establishmentDao.insert(est)
            }
        } catch (e: Exception) {
            Log.w("EstablishmentRepo", "Erro ao sincronizar estabelecimento: $establishmentId", e)
        }
    }

    /**
     * Retrieves an establishment by its ID from Firestore.
     *
     * @param establishmentId The ID of the establishment.
     * @return The Establishment object if found, null otherwise.
     */
    suspend fun getEstablishmentFromFirestoreById(establishmentId: String): Establishment? {
        val snapshot =
            firestore.collection("establishments").document(establishmentId).get().await()
        return snapshot.toObject(Establishment::class.java)
    }

    /**
     * Retrieves all establishments that a user has reviewed from Firestore.
     */
    suspend fun getUserEstablishments(userId: String, online: Boolean): List<Establishment> {
        return try {
            if (online) {
                val reviewSnapshot = firestore.collectionGroup("reviews")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val establishmentIds = reviewSnapshot.documents.mapNotNull { doc ->
                    doc.reference.parent.parent?.id
                }.toSet()

                val establishments = mutableListOf<Establishment>()
                for (id in establishmentIds) {
                    val snapshot = firestore.collection("establishments").document(id).get().await()
                    snapshot.toObject(Establishment::class.java)?.let { est ->
                        est.id = id
                        establishments.add(est)
                    }
                }

                saveEstablishmentsToRoom(establishments)
                saveUserEstablishmentsToRoom(userId, establishments)

                establishments
            } else {
                getUserEstablishmentsFromRoom(userId)
            }
        } catch (e: Exception) {
            Log.e("EstablishmentRepo", "Erro ao obter establishments do utilizador", e)
            return try {
                getUserEstablishmentsFromRoom(userId)
            } catch (inner: Exception) {
                inner.printStackTrace()
                emptyList()
            }
        }
    }

    private suspend fun saveUserEstablishmentsToRoom(userId: String, establishments: List<Establishment>) {
        if (establishments.isEmpty()) return
        val relations = establishments.map { est ->
            UserVisitedEntity(userId = userId, establishmentId = est.id)
        }
        userVisitedDao.insertAll(relations)
    }

    private suspend fun getUserEstablishmentsFromRoom(userId: String): List<Establishment> {
        val ids = userVisitedDao.getEstablishmentIdsForUser(userId)
        if (ids.isEmpty()) return emptyList()
        return establishmentDao.getByIds(ids)
    }


    /**
     * Retrieves the average rating of an establishment from Firestore.
     *
     * @param establishmentId The ID of the establishment.
     * @return The average rating as a Double.
     */
    suspend fun getAvgRatingFromFirestore(establishmentId: String): Double {
        return try {
            val doc = firestore.collection("establishments")
                .document(establishmentId)
                .get()
                .await()
            doc.getDouble("avgRating") ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}