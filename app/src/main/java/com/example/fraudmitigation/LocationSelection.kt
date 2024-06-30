package com.example.fraudmitigation

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
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
        val radioYes = findViewById<RadioButton>(R.id.radio_yes)
        val radioNo = findViewById<RadioButton>(R.id.radio_no)

        radioYes.isChecked = false;
        radioNo.isChecked = false;

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Check which radio button is selected
            when (checkedId) {
                R.id.radio_yes -> requestLocationPermission()
                R.id.radio_no -> Toast.makeText(this, "No Location Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestBackgroundLocationAccess()
        }

    }

    private fun requestBackgroundLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            updatePreferences();
            showConfirmationDialog("You have already granted the location access. To mitigate fraudulent transactions, we will notify you if " +
" + transactions occurs in your account approximately 5 kilometers away from your live location")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestBackgroundLocationAccess()
                } else {
                    Toast.makeText(this, "Foreground location access should be granted first for accessing the location in the background", Toast.LENGTH_SHORT).show()
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updatePreferences();
                    showConfirmationDialog("Thank you for granting the access. To mitigate fraudulent transactions, we will notify you if \" +\n" +
                            "                        \"transaction occurs in your account approximately 5 kilometers away from your live location")
                } else {
                    Toast.makeText(this, "Background location access denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showConfirmationDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, id ->
                // User clicked OK button
                dialog.dismiss()
                val intent = Intent(this, FraudMitigationActivity::class.java)
                startActivity(intent)
            }
        builder.create().show()
    }

    override fun onBackPressed() {
        val intent = Intent(this, FraudMitigationActivity::class.java)
        startActivity(intent)
    }


    private fun updatePreferences() {
        val preferences = getSharedPreferences(LocationSelection.PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_SELECTION, "Alert based on Live Location")
        editor.apply()
    }


}