package com.pvhung.ucar.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivitySplashBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity
import com.pvhung.ucar.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseBindingActivity<ActivitySplashBinding, SplashViewModel>() {

    private val TIME_ANIMATION = 5000L
    private val handlerGoMain = Handler(Looper.myLooper()!!)
    override val layoutId: Int
        get() = R.layout.activity_splash
    private val runnableGoMain = Runnable { this.goScreen() }

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()

    }

    private fun init() {

    }


    override fun setupData() {

    }

    override fun onStart() {
        super.onStart()
        handlerGoMain.postDelayed(runnableGoMain, TIME_ANIMATION)
    }

    override fun onResume() {
        super.onResume()
        changeStatusBarColorByScreen()
    }

    override fun onPause() {
        super.onPause()
        handlerGoMain.removeCallbacks(runnableGoMain)
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
}