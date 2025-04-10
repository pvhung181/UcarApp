package com.pvhung.ucar.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasLocationPermission(context: Context): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (!DeviceHelper.isMinSdk33) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(context: Activity, onGranted: () -> Unit, onFail: (() -> Unit)?) {
//        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
//            PermissionsManager(object : PermissionsListener {
//                override fun onExplanationNeeded(permissionsToExplain: List<String>) {}
//
//                override fun onPermissionResult(granted: Boolean) {
//                    if (granted) {
//                        onGranted.invoke()
//                    } else {
//                        onFail?.invoke()
//                    }
//                }
//
//            }).requestLocationPermissions(context)
//
//        }
    }

    fun requestLocationPermission(context: Activity) {
//        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
//            PermissionsManager(object : PermissionsListener {
//                override fun onExplanationNeeded(permissionsToExplain: List<String>) {}
//
//                override fun onPermissionResult(granted: Boolean) {
//
//                }
//
//            }).requestLocationPermissions(context)
//
//        }
    }

    fun requestNotificationPermission(context: Context) {

    }

    fun isGpsEnabled(context: Context): Boolean {
        return context.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
