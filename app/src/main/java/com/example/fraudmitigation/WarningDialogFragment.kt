package com.example.fraudmitigation

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class WarningDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Warning")
            .setMessage("Are you sure you want to proceed and set up Fraud Mitigation service?")
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(requireContext(), FraudMitigationActivity::class.java))
            }
            .setNegativeButton("No", null)
            .create()
    }
}