package com.tmt.community

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check for the custom data payload from our Cloud Function
        if (remoteMessage.data.containsKey("new_announcement")) {
            // --- THIS IS THE NEW, ROBUST LOGIC ---
            // Save a flag indicating a new announcement has arrived.
            // This works even if the app is in the background.
            val prefs = getSharedPreferences("CommUnityPrefs", Context.MODE_PRIVATE)
            prefs.edit { putBoolean("new_announcement_badge", true) }

            // Also send a broadcast for an instant update if the app is in the foreground
            val intent = Intent("new-announcement-event")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        // Handle the visible notification part
        remoteMessage.notification?.let {
            val title = it.title ?: "New Announcement"
            val message = it.body ?: "Check the app for details."
            sendNotification(title, message)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    // onNewToken remains the same
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // ...
    }
}