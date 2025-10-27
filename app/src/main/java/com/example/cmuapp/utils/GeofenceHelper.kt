package cmu.example.cmuapp.utils

import android.annotation.SuppressLint
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cmuapp.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Helper object to manage geofences with permission checks.
 */
object GeofenceHelper {

    /**
     * Checks if the app has location permissions.
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permissions from the user.
     */
    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            requestCode
        )
    }

    /**
     * Adds a geofence if location permissions are granted, otherwise requests permissions.
     */
    fun addGeofenceWithPermissionCheck(context: Context, lat: Double, lon: Double, id: String, name: String) {
        if (hasLocationPermission(context)) {
            addGeofence(context, lat, lon, id, name)
        } else {
            if (context is Activity) {
                requestLocationPermission(context, 1001)
            } else {
                println("⚠️ Sem permissão de localização para adicionar geofence")
            }
        }
    }

    /**
     * Adds a geofence at the specified location.
     */
    @SuppressLint("MissingPermission")
    private fun addGeofence(context: Context, lat: Double, lon: Double, id: String, name: String) {
        try {
            val geofence = Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lon, 50f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
                putExtra("ESTABLISHMENT_NAME", name)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val client = LocationServices.getGeofencingClient(context)
            client.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener { println("Geofence adicionada: $name") }
                .addOnFailureListener { e -> e.printStackTrace() }
        } catch (e: SecurityException) {
            println("⚠️ Não foi possível adicionar geofence: permissão negada")
            e.printStackTrace()
        }
    }
}
