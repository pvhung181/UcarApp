package com.pvhung.ucar.utils

import android.util.Patterns
import androidx.core.text.trimmedLength
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

object Validator {
    fun isPhoneNumberValid(phone: String): Boolean {
        return phone.trimmedLength() in (10..11) && Patterns.PHONE.matcher(phone).matches()
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isDateValid(d: Int, m: Int, y: Int): Boolean {
        if (DeviceHelper.isMinSdk26) {
            val currentDate = LocalDate.now()
            val selectedDate = LocalDate.of(y, m, d)
            if (selectedDate.isAfter(currentDate)) return false

            return ChronoUnit.YEARS.between(selectedDate, currentDate) >= 18

        } else {
            return isOver18(d, m, y)
        }
    }

    private fun isOver18(birthDay: Int, birthMonth: Int, birthYear: Int): Boolean {
        val today = Calendar.getInstance()

        val dob = Calendar.getInstance()
        dob.set(birthYear, birthMonth - 1, birthDay)

        val dobPlus18 = dob.clone() as Calendar
        dobPlus18.add(Calendar.YEAR, 18)

        return !today.before(dobPlus18)
    }

}