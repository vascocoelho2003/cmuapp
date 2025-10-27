import android.content.Context
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await
import android.util.Log

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Function to get the current location of the user.
 */
suspend fun getCurrentLocation(context: Context): LatLng? {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        Log.d("LocationDebug", "Permissão de localização não concedida")
        return null
    }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val location = fusedLocationClient.lastLocation.await()
        if (location != null) {
            Log.d("LocationDebug", "Localização obtida: ${location.latitude}, ${location.longitude}")
            LatLng(location.latitude, location.longitude)
        } else {
            Log.d("LocationDebug", "lastLocation é null, a pedir nova localização...")
            val locationTask = fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
            if (locationTask != null) {
                Log.d("LocationDebug", "Nova localização obtida: ${locationTask.latitude}, ${locationTask.longitude}")
                LatLng(locationTask.latitude, locationTask.longitude)
            } else {
                Log.d("LocationDebug", "Não foi possível obter localização")
                null
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        Log.e("LocationDebug", "SecurityException: ${e.message}")
        null
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("LocationDebug", "Exception: ${e.message}")
        null
    }
}

/**
 * Function to calculate the distance between two coordinates in meters using the Haversine formula.
 * Based on: https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
 */
fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (R * c).toFloat()
}
