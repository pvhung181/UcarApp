package com.pvhung.ucar.ui.driver

import android.graphics.Color
import android.os.Bundle
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivityDriverBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity

class DriverActivity : BaseBindingActivity<ActivityDriverBinding, DriverViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_driver

    override fun getViewModel(): Class<DriverViewModel> {
        return DriverViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()

    }

    private fun init() {

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
}