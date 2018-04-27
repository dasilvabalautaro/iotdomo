package com.hiddenodds.iotdomo.presentation.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.widget.Toast
import ch.zhaw.facerecognitionlibrary.R

class SettingsActivity: PreferenceActivity() {
    private var alertDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        alertDialog = AlertDialog.Builder(this)
        alertDialog!!.setTitle("Reset all settings")
        alertDialog!!.setIcon(android.R.drawable.ic_dialog_alert)

        alertDialog!!.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
            val preferences = PreferenceManager
                    .getDefaultSharedPreferences(applicationContext)
            val editor = preferences.edit()
            editor.clear()
            editor.apply()

            PreferenceManager.setDefaultValues(applicationContext,
                    R.xml.preferences, true)

            Toast.makeText(applicationContext, "Settings have been set to default.", Toast.LENGTH_SHORT).show()
        })

        alertDialog!!.setNegativeButton("OK",
                DialogInterface.OnClickListener { dialog, which ->})

        val button = Preference(this)
        button.onPreferenceClickListener = Preference
                .OnPreferenceClickListener {
                    alertDialog!!.show()
                    return@OnPreferenceClickListener true
        }
    }
}