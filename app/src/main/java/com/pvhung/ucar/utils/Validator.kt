package com.pvhung.ucar.utils

import android.util.Patterns
import androidx.core.text.trimmedLength

object Validator {
    fun isPhoneNumberValid(phone: String): Boolean {
        return phone.trimmedLength() in (10..11) && Patterns.PHONE.matcher(phone).matches()
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}