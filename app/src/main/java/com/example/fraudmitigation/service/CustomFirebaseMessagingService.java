package com.example.fraudmitigation.service;


import android.util.Log;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CustomFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Received message from backend");

        // Check if message contains notification data
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification(remoteMessage.getNotification());
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }

    /*private void sendNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, *//* YourMainActivity.class *//*.class); // Replace with your main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, *//* CHANNEL_ID *//*)
                .setSmallIcon(R.drawable*//* Your notification icon resource *//*)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel required for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }

        notificationManager.notify(*//* Notification ID *//*, notificationBuilder.build());
    }*/

    private void handleDataPayload(Map<String, String> data) {
        // Extract data from payload and perform actions accordingly
        String key1 = data.get("key1");
        String key2 = data.get("key2");

        // Example handling based on data
        if (key1.equals("value1")) {
            // Perform action specific to key1
        } else if (key2.equals("value2")) {
            // Perform action specific to key2
            //
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(TAG, "Generated new Token" + token);
        super.onNewToken(token);
    }
}