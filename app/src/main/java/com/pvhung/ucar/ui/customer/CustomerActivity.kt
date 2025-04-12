package com.pvhung.ucar.ui.customer

import android.graphics.Color
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivityCustomerBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity

class CustomerActivity : BaseBindingActivity<ActivityCustomerBinding, CustomerViewModel>() {

    private var navController: NavController? = null
    private var navGraph: NavGraph? = null

    override val layoutId: Int
        get() = R.layout.activity_customer

    override fun getViewModel(): Class<CustomerViewModel> {
        return CustomerViewModel::class.java
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
        navController?.setGraph(R.navigation.customer_nav)
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