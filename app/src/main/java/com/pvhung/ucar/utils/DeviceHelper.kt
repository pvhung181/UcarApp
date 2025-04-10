package com.pvhung.ucar.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.TextUtils


object DeviceHelper {

    val isMinSdk29 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val isMinSdk26 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val isMinSdk24 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isMinSdk27 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    val isMaxSdk29 get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    val isMinSdk30 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val isMinSdk33 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}