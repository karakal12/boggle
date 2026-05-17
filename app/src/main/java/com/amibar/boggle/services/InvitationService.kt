package com.amibar.boggle.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.ui.mainmenu.MainActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service that handles Firebase Cloud Messaging (FCM) messages.
 * It is primarily used for receiving and displaying game invitations as system notifications.
 */
class InvitationService : FirebaseMessagingService() {
    /**
     * Called when a new FCM registration token is generated for the device.
     * Updates the token in the Firebase Realtime Database for the current user.
     * @param token The new registration token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        if (FirebaseHandler.currentUser != null) {
            FirebaseHandler.userRef?.child("fcmToken")?.setValue(token)
        }
    }

    /**
     * Called when a message is received from FCM.
     * Parses the message data, deletes the invitation record from the database,
     * and displays a local notification.
     * @param remoteMessage The received message object.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)

        val data = remoteMessage.getData()


        // Delete the invitation from the database now that it's received to avoid stale invites
        if (data.containsKey("invitationId")) {
            val invitationId = data["invitationId"]
            deleteInvitation(invitationId!!)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            val title = remoteMessage.getNotification()!!.title
            val body = remoteMessage.getNotification()!!.body
            showNotification(title, body, data)
        } else if (data.isNotEmpty()) {
            // Handle data-only payload if notification block is missing
            val title = if (data.containsKey("title")) data["title"] else "New Game Invitation"
            val body =
                if (data.containsKey("body")) data["body"] else "Someone invited you to play Boggle!"
            showNotification(title, body, data)
        }
    }

    /**
     * Removes an invitation entry from the Firebase Realtime Database.
     * @param invitationId The unique ID of the invitation to delete.
     */
    private fun deleteInvitation(invitationId: String) {
        val currentUserId = FirebaseAuth.getInstance().uid
        if (currentUserId != null) {
            FirebaseHandler.rootRef
                .child("invitations")
                .child(currentUserId)
                .child(invitationId)
                .removeValue()
                .addOnSuccessListener(OnSuccessListener { aVoid: Void? ->
                    Log.d(
                        TAG,
                        "Invitation deleted from DB: $invitationId"
                    )
                })
                .addOnFailureListener { e: Exception? ->
                    Log.e(
                        TAG,
                        "Failed to delete invitation",
                        e
                    )
                }
        }
    }

    /**
     * Builds and displays a system notification for the game invitation.
     * Includes a PendingIntent that opens MainActivity with the room code.
     * @param title Notification title.
     * @param body  Notification body text.
     * @param data  Data payload containing game room details.
     */
    private fun showNotification(
        title: String?,
        body: String?,
        data: MutableMap<String?, String?>?
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        // Pass room code and action if present to allow joining directly from notification
        if (data != null && data.containsKey("roomCode")) {
            intent.putExtra("roomCode", data["roomCode"])
            intent.putExtra("action", "join")
        }
        if (data != null && data.containsKey("invitationId")) {
            intent.putExtra("invitationId", data["invitationId"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Invitations",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        /** Tag used for logging.  */
        private const val TAG = "InvitationService"

        /** Notification channel ID for game invitations.  */
        private const val CHANNEL_ID = "invitation_channel"
    }
}
