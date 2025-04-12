package com.pvhung.ucar.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivitySplashBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity
import com.pvhung.ucar.ui.driver.DriverActivity
import com.pvhung.ucar.ui.main.MainActivity
import com.pvhung.ucar.utils.FirebaseDatabaseUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseBindingActivity<ActivitySplashBinding, SplashViewModel>() {

    override val layoutId: Int
        get() = R.layout.activity_splash

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()

    }

    private fun init() {
        if (FirebaseDatabaseUtils.isUserAlreadyLogin()) {
            goDriverScreen()
        } else {
            goScreen()
        }
    }


    override fun setupData() {

    }


    override fun onResume() {
        super.onResume()
        changeStatusBarColorByScreen()
    }


    private fun changeStatusBarColorByScreen() {
        val colorS = Color.TRANSPARENT
        val colorB = Color.BLACK
        changeSystemUIColor(
            colorS,
            colorB,
            darkStatusBar = false,
            darkNavigation = false,
            true
        )
    }


    private fun goScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goDriverScreen() {
        startActivity(Intent(this, DriverActivity::class.java))
        finish()
    }
}