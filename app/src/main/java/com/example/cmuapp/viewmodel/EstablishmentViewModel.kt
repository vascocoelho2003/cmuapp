package com.example.cmuapp.viewmodel

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.example.cmuapp.utils.GeofenceHelper
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.data.repository.EstablishmentRepository
import com.example.cmuapp.utils.BatteryUtils.isBatteryLow
import com.example.cmuapp.utils.isOnline
import dagger.hilt.android.lifecycle.HiltViewModel
import getCurrentLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel to manage Establishment data and operations.
 */
@HiltViewModel
class EstablishmentViewModel @Inject constructor(
    private val repository: EstablishmentRepository,
) : ViewModel() {
    private val _establishments = MutableStateFlow<List<Establishment>>(emptyList())
    val establishments: StateFlow<List<Establishment>> = _establishments
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _userEstablishments = MutableStateFlow<List<Establishment>>(emptyList())
    val userEstablishments: StateFlow<List<Establishment>> = _userEstablishments

    /**
     * Fetch establishments from the repository, update geofences, and manage loading state.
     */
    fun fetchEstablishments(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val location = getCurrentLocation(context)
                val lat = location?.latitude ?: 40.442492
                val lon = location?.longitude ?: -79.942553

                val places = if (isOnline(context) && !isBatteryLow(context)) {
                    val apiPlaces = repository.fetchEstablishment("$lat,$lon")

                    val apiPlacesWithRating = apiPlaces.map { est ->
                        val rating = repository.getAvgRatingFromFirestore(est.id)
                        est.copy(avgRating = rating)
                    }

                    apiPlacesWithRating.forEach { est ->
                        repository.saveEstablishmentToFirestore(est)
                    }
                    repository.saveEstablishmentsToRoom(apiPlacesWithRating)

                    apiPlacesWithRating
                } else {
                    withContext(Dispatchers.IO) {
                        repository.getAllFromRoom()
                    }
                }
                _establishments.value = places
                val hasLocationPermission = GeofenceHelper.hasLocationPermission(context)
                if (hasLocationPermission) {
                    places.forEach { est ->
                        if (!isBatteryLow(context)) {
                            GeofenceHelper.addGeofenceWithPermissionCheck(context, est.lat ?: 0.0, est.lon ?: 0.0, est.id, est.name)
                        } else {
                            println("⚠️ Bateria baixa – geofence não adicionada para economizar energia")
                        }
                    }
                }

                println("⚡ Carregados ${places.size} establishments (online=${isOnline(context)})")

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get leaderboard of establishments based on rating type and current location.
     */
    suspend fun getLeaderboard(context: Context, ratingType: String): List<Establishment> {
        val location = getCurrentLocation(context)
        val lat = location?.latitude ?: 40.442492
        val lon = location?.longitude ?: -79.942553

        val online = isOnline(context)
        return repository.getNearbyLeaderboard(lat, lon, ratingType, online)
    }

    /**
     * Synchronize and get an establishment by its ID.
     */
    fun syncAndGetEstablishmentById(establishmentId: String): StateFlow<Establishment?> {
        val state = MutableStateFlow<Establishment?>(null)
        viewModelScope.launch {
            repository.syncEstablishmentFromFirestore(establishmentId)
            state.value = repository.getEstablishmentById(establishmentId)
        }
        return state
    }

    /**
     * Load establishments associated with a specific user.
     */
    fun loadUserEstablishments(userId: String, context: Context) {
        viewModelScope.launch {
            val online = isOnline(context)
            val establishments = repository.getUserEstablishments(userId, online)
            _userEstablishments.value = establishments
        }
    }


}
