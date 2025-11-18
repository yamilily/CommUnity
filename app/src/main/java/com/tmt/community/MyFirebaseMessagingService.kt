package com.tmt.community

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function is called when a message is received.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val intent = Intent("new-announcement-event")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        // --- HANDLE THE NOTIFICATION PAYLOAD FOR THE SYSTEM TRAY ---
        remoteMessage.notification?.let {
            val title = it.title ?: "New Announcement"
            val message = it.body ?: "Check the app for details."
            sendNotification(title, message)
        }
    }

    // This function is called when a new FCM token is generated.
    // It's now correctly placed outside of sendNotification.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        // You can send this token to your server if needed
    }

    private fun sendNotification(title: String, message: String) {
        // Create an Intent to open MainActivity when the notification is tapped.
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("title", title)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel" // Your notification channel ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications) // Make sure you have this icon in your drawable folder
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo (API 26), notification channels are required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Announcements", // Channel name visible to the user
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification. The ID (0) can be any unique integer.
        notificationManager.notify(0, notificationBuilder.build())
    }
}