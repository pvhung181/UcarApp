package com.pvhung.ucar.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

object IntentManager {
    fun goToEnableGps(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}