package com.example.fraudmitigation.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.fraudmitigation.CustomerGeoCode;
import com.example.fraudmitigation.geofencing.RadiusValidator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

    private LocationCallback mLocationCallback;


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
            try {
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
        }
    }

    @SuppressLint("MissingPermission")
    private CustomerGeoCode fetchLocation() {
        AtomicReference<Double> latitude = new AtomicReference<>();
        AtomicReference<Double> longitude = new AtomicReference<>();

        requestNewLocationData();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location mLastLocation = locationResult.getLastLocation();
                if (mLastLocation != null) {
                    latitude.set(mLastLocation.getLatitude());
                    longitude.set(mLastLocation.getLongitude());
                }
            }
        };

        Toast.makeText(CustomFirebaseMessagingService.this, latitude.get() + " " + longitude.get(), Toast.LENGTH_SHORT).show();
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
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("token", token);

        DocumentReference docRef = db.collection(deviceTokenId).document(tokenIdPair);

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

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            fusedLocationClient.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    Looper.myLooper()
            );
        }

    }
}