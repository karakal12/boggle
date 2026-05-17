package com.amibar.boggle.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.ui.mainmenu.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Service that handles Firebase Cloud Messaging (FCM) messages.
 * It is primarily used for receiving and displaying game invitations as system notifications.
 */
public class InvitationService extends FirebaseMessagingService {
    /** Tag used for logging. */
    private static final String TAG = "InvitationService";
    /** Notification channel ID for game invitations. */
    private static final String CHANNEL_ID = "invitation_channel";

    /**
     * Called when a new FCM registration token is generated for the device.
     * Updates the token in the Firebase Realtime Database for the current user.
     * @param token The new registration token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        if (FirebaseHandler.getAuth().getCurrentUser() != null){
            FirebaseHandler.getInstance().getUserRef().child("fcmToken").setValue(token);
        }
    }

    /**
     * Called when a message is received from FCM.
     * Parses the message data, deletes the invitation record from the database,
     * and displays a local notification.
     * @param remoteMessage The received message object.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        
        // Delete the invitation from the database now that it's received to avoid stale invites
        if (data.containsKey("invitationId")) {
            String invitationId = data.get("invitationId");
            deleteInvitation(invitationId);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body, data);
        } else if (data.size() > 0) {
            // Handle data-only payload if notification block is missing
            String title = data.containsKey("title") ? data.get("title") : "New Game Invitation";
            String body = data.containsKey("body") ? data.get("body") : "Someone invited you to play Boggle!";
            showNotification(title, body, data);
        }
    }

    /**
     * Removes an invitation entry from the Firebase Realtime Database.
     * @param invitationId The unique ID of the invitation to delete.
     */
    private void deleteInvitation(String invitationId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            FirebaseHandler.getInstance().getRootRef()
                    .child("invitations")
                    .child(currentUserId)
                    .child(invitationId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Invitation deleted from DB: " + invitationId))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete invitation", e));
        }
    }

    /**
     * Builds and displays a system notification for the game invitation.
     * Includes a PendingIntent that opens MainActivity with the room code.
     * @param title Notification title.
     * @param body  Notification body text.
     * @param data  Data payload containing game room details.
     */
    private void showNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Pass room code and action if present to allow joining directly from notification
        if (data != null && data.containsKey("roomCode")) {
            intent.putExtra("roomCode", data.get("roomCode"));
            intent.putExtra("action", "join");
        }
        if (data != null && data.containsKey("invitationId")) {
            intent.putExtra("invitationId", data.get("invitationId"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Game Invitations",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
