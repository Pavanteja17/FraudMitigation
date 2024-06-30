package com.example.fraudmitigation

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.fraudmitigation.ui.notifications.NotificationsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices


class LocationSelection : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2
        private const val PREFS_NAME = "FraudMitigationPrefs"
        private const val KEY_SELECTION = "key_selection"
        private const val KEY_TOGGLE = "key_toggle"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selection)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Remove the default title to use custom TextView
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        val radioYes = findViewById<View>(R.id.radio_yes)
        val radioNo = findViewById<View>(R.id.radio_no)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Check which radio button is selected
            when (checkedId) {
                R.id.radio_yes -> requestLocationPermission()
                R.id.radio_no -> Toast.makeText(this, "No Location Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)

            updatePreferences()

        } else {
            //TODO:
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                showConfirmationDialog("Location is granted while using the app");

            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Background location permission granted
                showConfirmationDialog("Thank you for granting the access. To mitigate fraudulent transactions, we will notify you if " +
                        "transaction occurs in your account approximately 5 kilometers away from your live location");
            } else {
                // Permission denied
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmationDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, id ->
                // User clicked OK button
                dialog.dismiss()
            }
        builder.create().show()
    }


    private fun updatePreferences() {
        val preferences = getSharedPreferences(LocationSelection.PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_SELECTION, "Alert based on Live Location")
        editor.apply()
    }


}