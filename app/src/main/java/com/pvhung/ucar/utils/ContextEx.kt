package com.pvhung.ucar.utils

import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager


val Context.locationManager: LocationManager
    get() = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

val Context.notificationManager: NotificationManager
    get() = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
