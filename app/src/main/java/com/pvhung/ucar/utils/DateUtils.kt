package com.pvhung.ucar.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun simpleFormatDate(d: Int, m: Int, y: Int): String {
        val dd = "${if (d < 10) 0 else ""}$d"
        val mm = "${if (m < 10) 0 else ""}$m"
        return "${dd}/${mm}/${y}"
    }

    fun convertMillisToFormattedDate(millis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
        }

        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Chủ nhật"
            Calendar.MONDAY -> "Thứ hai"
            Calendar.TUESDAY -> "Thứ ba"
            Calendar.WEDNESDAY -> "Thứ tư"
            Calendar.THURSDAY -> "Thứ năm"
            Calendar.FRIDAY -> "Thứ sáu"
            Calendar.SATURDAY -> "Thứ bảy"
            else -> ""
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(millis))

        return "$dayOfWeek, $dateString"
    }
}