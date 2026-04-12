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

public class InvitationService extends FirebaseMessagingService {
    private static final String TAG = "InvitationService";
    private static final String CHANNEL_ID = "invitation_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        if (FirebaseHandler.getAuth().getCurrentUser() != null){
            FirebaseHandler.getInstance().getUserRef().child("fcmToken").setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        
        // Delete the invitation from the database now that it's received
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
            // Handle data payload if notification is null
            String title = "New Game Invitation";
            String body = "Someone invited you to play Boggle!";
            showNotification(title, body, data);
        }
    }

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

    private void showNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Pass room code if present
        if (data != null && data.containsKey("roomCode")) {
            intent.putExtra("roomCode", data.get("roomCode"));
            intent.putExtra("action", "join");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
