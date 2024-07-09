package com.example.fraudmitigation.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.fraudmitigation.CustomerGeoCode;
import com.example.fraudmitigation.MainActivity;
import com.example.fraudmitigation.R;
import com.example.fraudmitigation.geofencing.RadiusValidator;
import com.example.fraudmitigation.ui.cardsettings.CardSettingsActivity;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CustomFirebaseMessagingService";

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback mLocationCallback;

    private HandlerThread handlerThread;

    private Handler handler;
    private Timer retryTimer;

    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_LOCATION = "location";


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

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String location = prefs.getString(KEY_LOCATION, null);
                CustomerGeoCode customerGeoCode = null;
                if (location != null) {
                    // Use the location as needed
                    String[] latLng = location.split(",");
                    double custLatitude = Double.parseDouble(latLng[0]);
                    double custLongitude = Double.parseDouble(latLng[1]);
                    Log.d("cust Location: ", "Location: " + custLatitude + ", " + custLongitude);
                    customerGeoCode = new CustomerGeoCode(custLatitude, custLongitude);

                    Log.d("POS/ATM location", "Location: " + latitude + ", " + longitude);
                }

                // CustomerGeoCode customerGeoCode = fetchLocation();

                boolean withinRadius = RadiusValidator.isWithinRadius(Double.parseDouble(latitude), Double.parseDouble(longitude), customerGeoCode.getLatitude(), customerGeoCode.getLongitude());
                Log.d("LocationRadius", "Radius status: " + withinRadius);
                double v = RadiusValidator.calculateHaversineDistance(Double.parseDouble(latitude), Double.parseDouble(longitude), customerGeoCode.getLatitude(), customerGeoCode.getLongitude());
                if(!withinRadius){
                    sendNotification("Fraud Detected", "Your card was swiped " + v +  " KM from your current location. If this was you, please ignore this notification. Otherwise, immediately contact 000-800-001-6090 to report the transaction as fraudulent and block your account to prevent further unauthorized activities.");
                }
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

       /* mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location mLastLocation = locationResult.getLastLocation();
                if (mLastLocation != null) {
                    latitude.set(mLastLocation.getLatitude());
                    longitude.set(mLastLocation.getLongitude());
                }
            }
        };*/
        //requestNewLocationData();


        // Toast.makeText(CustomFirebaseMessagingService.this, latitude.get() + " " + longitude.get(), Toast.LENGTH_SHORT).show();
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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        //if (ActivityCompat.checkSelfPermission(this,
        // Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request both permissions here
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            Looper looper = handlerThread.getLooper();
            if (looper == null) {
                Log.e("Location", "Looper is null!");
                return;
            } /*else {
                fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, looper);
            }*/

            try {
                fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, looper);
                if (retryTimer != null) {
                    retryTimer.cancel(); // Cancel retries if successful
                }
            } catch (Exception e) {
                Log.e("Location", "Failed to request location updates: " + e.getMessage());
                if (retryTimer == null) {
                    retryTimer = new Timer();
                    retryTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, looper);
                        }
                    }, 1000, 2000); // Initial delay, then repeat every 2 seconds
                }
            }
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "location_updates";

        // Create notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Location Updates";
            String channelDescription = "Notifications for location updates";
            int channelImportance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Replace with your own icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create an intent that will open the MainActivity when the notification is clicked
        Intent intent = new Intent(this, CardSettingsActivity.class);
        intent.putExtra("isFraud", true);
        intent.putExtra("message", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        notificationBuilder.setContentIntent(pendingIntent);

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread = new HandlerThread("LocationThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void logOnLooper(String message) {
        handler.post(() -> Log.d("Location info from looper thread: ", message));
    }
}