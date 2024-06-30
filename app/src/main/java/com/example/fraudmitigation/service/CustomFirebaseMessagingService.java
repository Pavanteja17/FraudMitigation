package com.example.fraudmitigation.service;


import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;


import androidx.annotation.NonNull;

import com.example.fraudmitigation.CustomerGeoCode;
import com.example.fraudmitigation.geofencing.RadiusValidator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CustomFirebaseMessagingService";

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Received message from backend");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if message contains notification data
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification(remoteMessage.getNotification());
        }

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            try
            {
                Map<String, String> params = remoteMessage.getData();
                JSONObject object = new JSONObject(params);
                Log.e("JSON OBJECT", object.toString());
                String latitude = object.getString("latitude");
                String longitude = object.getString("longitude");

                CustomerGeoCode customerGeoCode = fetchLocation();

                boolean withinRadius = RadiusValidator.isWithinRadius(Double.parseDouble(latitude), Double.parseDouble(longitude), customerGeoCode.getLatitude(), customerGeoCode.getLongitude());
            } catch (JSONException e) {
                //TODO
            }
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }

    @SuppressLint("MissingPermission")
    private CustomerGeoCode fetchLocation() {
        AtomicReference<Double> latitude = new AtomicReference<>();
        AtomicReference<Double> longitude = new AtomicReference<>();
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                latitude.set(location.getLatitude());
                longitude.set(location.getLongitude());
                Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
            } else {
                Log.d("Location", "Location not found");
            }
        });
        return new CustomerGeoCode(latitude.get(), longitude.get());
    }


    private void sendNotification(RemoteMessage.Notification notification) {
        /*Intent intent = new Intent(this,  MainActivity.class); // Replace with your main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  Request code , intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,  CHANNEL_ID )
                .setSmallIcon(R.drawable Your notification icon resource )
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

        notificationManager.notify( Notification ID , notificationBuilder.build());*/
    }

    private void handleDataPayload(Map<String, String> data) {
        // Extract data from payload and perform actions accordingly
        String key1 = data.get("key1");
        String key2 = data.get("key2");
    }

    @Override
    public void onNewToken(@NonNull String token) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String deviceTokenId = "deviceTokenId";
        String tokenIdPair = "tokenIdPair";
        String email = "user@example.com";

        storeTokenData(deviceTokenId, tokenIdPair, email, token, db);
        super.onNewToken(token);
    }

    private void storeTokenData(String deviceTokenId, String tokenIdPair, String email, String token, FirebaseFirestore db) {
        // Create a map to store the data
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("token", token);

        // Create a reference to the document
        DocumentReference docRef = db.collection(deviceTokenId).document(tokenIdPair);

        // Set the data in the document
        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Data successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error writing document", e);
                    }
                });
    }
}