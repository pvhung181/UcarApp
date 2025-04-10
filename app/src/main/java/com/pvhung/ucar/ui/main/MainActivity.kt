package com.pvhung.ucar.ui.main

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.databinding.ActivityMainBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity

class MainActivity : BaseBindingActivity<ActivityMainBinding, MainViewModel>() {
    private var navController: NavController? = null
    private var navGraph: NavGraph? = null

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initView()
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
        navController?.setGraph(R.navigation.main_nav)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeSystemUIColor(Color.TRANSPARENT, Color.TRANSPARENT, false, false, true)
    }

}