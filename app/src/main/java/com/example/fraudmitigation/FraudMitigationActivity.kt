package com.example.fraudmitigation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class FraudMitigationActivity : AppCompatActivity() {
    private var currentSelectionTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fraud_mitigation)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Remove the default title to use custom TextView
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { onBackPressed() }

        currentSelectionTextView = findViewById<TextView>(R.id.text_area)
        val updateSelectionButton = findViewById<Button>(R.id.update_selection_button)
        val fraudMitigationSwitch = findViewById<Switch>(R.id.toggle_fraud_mitigation)

        loadPreferences(fraudMitigationSwitch)

        updateSelectionButton.setOnClickListener(View.OnClickListener { updateSelection(fraudMitigationSwitch) })

        fraudMitigationSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> savePreferences(fraudMitigationSwitch) })
    }

    private fun loadPreferences(fraudMitigationSwitch: Switch?) {
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val selection = preferences.getString(KEY_SELECTION, "No selection")
        val isToggleOn = preferences.getBoolean(KEY_TOGGLE, false)

        currentSelectionTextView!!.text = selection
        fraudMitigationSwitch!!.isChecked = isToggleOn
    }

    private fun savePreferences(fraudMitigationSwitch: Switch?) {
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString(KEY_SELECTION, currentSelectionTextView!!.text.toString())
        editor.putBoolean(KEY_TOGGLE, fraudMitigationSwitch!!.isChecked)
        editor.apply()
    }

    private fun updateSelection(fraudMitigationSwitch: Switch?) {
        val intent = Intent(
            this,
            LocationSelection::class.java
        )
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    companion object {
        private const val PREFS_NAME = "FraudMitigationPrefs"
        private const val KEY_SELECTION = "key_selection"
        private const val KEY_TOGGLE = "key_toggle"
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}