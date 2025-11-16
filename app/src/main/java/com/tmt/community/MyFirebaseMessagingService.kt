package com.tmt.community

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log // <--- IMPORT ADDED
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function is called when a message is received while the app is in the foreground.
    // Your existing code here is correct.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            if (title != null && body != null) {
                val newAnnouncementObject = Announcement(title = title, body = body)
                AnnouncementHolder.newAnnouncement.postValue(newAnnouncementObject)
                sendNotification(title, body)
            }
        }
    }

    // This is your existing, correct function to build the notification.
    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.default_notification_channel_id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Announcements Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }

    // --- THIS IS THE MISSING PIECE ---
    // This function is automatically called by Firebase when a new token is generated.
    // We will use it to log the token for our direct test.
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Log the token to Logcat with a specific tag so we can find it easily.
        Log.d("FCM_TOKEN", "Refreshed token: $token")
    }
}