package com.pvhung.ucar.ui.driver

import android.graphics.Color
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.util.Util
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivityDriverBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity
import com.pvhung.ucar.utils.Utils

class DriverActivity : BaseBindingActivity<ActivityDriverBinding, DriverViewModel>() {

    private var navController: NavController? = null
    private var navGraph: NavGraph? = null

    override val layoutId: Int
        get() = R.layout.activity_driver

    override fun getViewModel(): Class<DriverViewModel> {
        return DriverViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initView()
    }

    private fun init() {

    }


    override fun setupData() {

    }

    private fun initView() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        navController?.let {
            it.addOnDestinationChangedListener { _: NavController?, _: NavDestination, _: Bundle? ->
            }
        }
        navController?.setGraph(R.navigation.driver_nav)
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

    override fun onDestroy() {
        super.onDestroy()
        Utils.removeDriverWorking()
        Utils.removeDriverAvailable()
    }
}