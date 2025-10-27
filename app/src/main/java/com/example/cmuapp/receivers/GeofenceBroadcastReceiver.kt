package com.example.cmuapp.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cmuapp.MainActivity
import com.example.cmuapp.utils.BatteryUtils
import com.example.cmuapp.utils.NotificationUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.Calendar

/**
 * BroadcastReceiver to handle geofence transitions and send notifications.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        println("⚡ Geofence event received")

        if (!NotificationUtils.isNotificationsEnabled(context)) {
            println("🔕 Notificações desativadas pelo utilizador")
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isPowerSaveMode = powerManager.isPowerSaveMode
        if (BatteryUtils.isBatteryLow(context) || isPowerSaveMode || hour !in 16..18) return

        val establishmentName = intent.getStringExtra("ESTABLISHMENT_NAME") ?: "Estabelecimento"

        val prefs = context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
        val notifiedIds = prefs.getStringSet("notified_ids", mutableSetOf()) ?: mutableSetOf()

        if (!notifiedIds.contains(establishmentName)) {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Estabelecimento por perto")
                .setContentText("Estás a menos de 50 metros de $establishmentName!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)

            notifiedIds.add(establishmentName)
            prefs.edit().putStringSet("notified_ids", notifiedIds).apply()
        }
    }
}
