package com.pvhung.ucar.utils

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object OnBackPressed {
    fun onBackPressedFinishActivity(
        fragmentActivity: FragmentActivity,
        fragment: Fragment,
        callback: (() -> Unit)? = null
    ) {
        fragmentActivity.onBackPressedDispatcher.addCallback(
            fragment,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    callback?.invoke()
                    fragmentActivity.finish()
                }
            })
    }
}