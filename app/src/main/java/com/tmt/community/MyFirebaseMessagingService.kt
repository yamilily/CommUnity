package com.tmt.community

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // This part correctly checks for a 'data' payload.
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            // We must check that title and body are not null
            if (title != null && body != null) {

                // --- THIS IS THE FIX ---
                // 1. Create a full Announcement object from the incoming data.
                val newAnnouncementObject = Announcement(title = title, body = body)

                // 2. Post the entire OBJECT to the AnnouncementHolder.
                // This now matches the expected type (Announcement?).
                AnnouncementHolder.newAnnouncement.postValue(newAnnouncementObject)

                // 3. The rest of the function continues as normal.
                sendNotification(title, body)
            }
        }
    }

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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}