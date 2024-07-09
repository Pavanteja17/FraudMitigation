package com.example.fraudmitigation.ui.cardsettings

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.example.fraudmitigation.R


class CardSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Remove the default title to use custom TextView
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // In your activity or fragment
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_custom, null)

        val dialogBuilder: AlertDialog.Builder =
            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        dialogBuilder.setView(dialogView)

        val toggleFraudMitigation = findViewById<Switch>(R.id.toggle_fraud_mitigation)
        val toggleFraudMitigation2 = findViewById<Switch>(R.id.toggle_fraud_mitigation2)
        val toggleFraudMitigation3 = findViewById<Switch>(R.id.toggle_fraud_mitigation3)


        toggleFraudMitigation.setOnClickListener { v: View? ->
            toggleFraudMitigation2.isChecked = false
            toggleFraudMitigation3.isChecked = false

        }

        val title: TextView = dialogView.findViewById(R.id.dialog_title)
        val message: TextView = dialogView.findViewById(R.id.dialog_message)
        val buttonOk: Button = dialogView.findViewById(R.id.dialog_button_ok)

        title.text = "Alert"
        message.text = "Your card was swiped 1 KM from your current location. If this was you, please ignore this notification. Otherwise, immediately contact 000-800-001-6090 to report the transaction as fraudulent and block your account to prevent further unauthorized activities"

        val alertDialog: AlertDialog = dialogBuilder.create()

        buttonOk.setOnClickListener { v: View? -> alertDialog.dismiss() }

        if (getIntent().getBooleanExtra("isFraud", false)) {
            message.text = getIntent().getStringExtra("message")
            alertDialog.show()

        }

    }
}