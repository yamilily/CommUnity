package com.tmt.community // Make sure this matches your package name!

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This is called when a message is received.
    // With Data messages, this function is called whether the app is in the foreground OR background!
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // We get the title and body from the "data" payload we sent from the console
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        // We need both to proceed
        if (title != null && body != null) {
            // Job 1: Show the push notification
            sendNotification(title, body)

            // Job 2: Broadcast the new announcement to the app's UI
            // We use our "radio station" to send the message.
            // postValue is important because we are on a background thread.
            AnnouncementHolder.newAnnouncement.postValue("$title: $body")
        }
    }

    // This is a new function to build and show the notification
    private fun sendNotification(title: String, messageBody: String) {
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_dashboard_black_24dp) // Default notification icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // Removes the notification when tapped

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo, notification channel is required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Town Announcements",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // The number 0 is the ID of the notification.
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "The new token is: $token")
    }
}