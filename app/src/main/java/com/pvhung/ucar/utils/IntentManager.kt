package com.pvhung.ucar.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object IntentManager {
    fun goToEnableGps(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun goToCall(activity: Activity, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber}")
        }
        activity.startActivity(intent)
    }

    fun goToMessage(activity: Activity, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:${phoneNumber}")
            putExtra("sms_body", "")
        }
        activity.startActivity(intent)
    }

}