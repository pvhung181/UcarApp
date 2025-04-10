package com.pvhung.ucar.utils

object DateUtils {
    fun simpleFormatDate(d: Int, m: Int, y: Int): String {
        val dd = "${if(d < 10) 0 else ""}$d"
        val mm = "${if(m < 10) 0 else ""}$m"
        return "${dd}/${mm}/${y}"
    }
}