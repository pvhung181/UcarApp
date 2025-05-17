package com.pvhung.ucar.utils

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.pvhung.ucar.R

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun Button.setOnline() {
    backgroundTintList = ColorStateList.valueOf(getColor(context, R.color.color_ED4634))
    text = ContextCompat.getString(context, R.string.go_to_offline)
}

fun Button.setOffline() {
    backgroundTintList = ColorStateList.valueOf(getColor(context, R.color.color_3BCC27))
    text = ContextCompat.getString(context, R.string.go_to_online)
}
