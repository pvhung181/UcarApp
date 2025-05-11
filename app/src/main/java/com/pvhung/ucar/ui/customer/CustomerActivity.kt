package com.pvhung.ucar.ui.customer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.androidnetworking.AndroidNetworking
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.ActivityCustomerBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity
import com.pvhung.ucar.ui.main.MainViewModel

class CustomerActivity : BaseBindingActivity<ActivityCustomerBinding, MainViewModel>() {

    private var navController: NavController? = null
    private var navGraph: NavGraph? = null

    override val layoutId: Int
        get() = R.layout.activity_customer

    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initView()
        AndroidNetworking.initialize(applicationContext)

    }

    private fun init() {

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent?.data!!.getQueryParameter("opType") == "payment") {
            viewModel.captureOrder.value = true
        } else if (intent?.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }
}