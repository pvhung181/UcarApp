package com.pvhung.ucar.ui.purchase

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
import com.pvhung.ucar.databinding.ActivityPurchaseBinding
import com.pvhung.ucar.ui.base.BaseBindingActivity
import com.pvhung.ucar.ui.main.MainViewModel

class PurchaseActivity : BaseBindingActivity<ActivityPurchaseBinding, MainViewModel>() {


    override val layoutId: Int
        get() = R.layout.activity_purchase

    override fun getViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initView()
        finish()
    }

    override fun setupData() {

    }

    private fun initView() {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeSystemUIColor(Color.TRANSPARENT, Color.TRANSPARENT, false, false, true)
    }

}