package com.pvhung.ucar.utils

import com.google.firebase.database.DataSnapshot

fun DataSnapshot.getStringValue(): String {
    return this.getValue(String::class.java) ?: ""
}

fun DataSnapshot.getBooleanValue(): Boolean {
    return this.getValue(Boolean::class.java) ?: false
}

fun DataSnapshot.getNumberValue(): Float {
    return this.getValue(Float::class.java) ?: 0f
}