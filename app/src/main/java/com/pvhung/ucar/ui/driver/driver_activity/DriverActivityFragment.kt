package com.pvhung.ucar.ui.driver.driver_activity

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentDriverActivityBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class DriverActivityFragment : BaseBindingFragment<FragmentDriverActivityBinding, DriverActivityViewModel>() {

    override fun getViewModel(): Class<DriverActivityViewModel> {
        return DriverActivityViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_activity

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        init()
        initView()
        onClick()
    }

    private fun init() {

    }

    private fun initView() {

    }

    override fun observerData() {

    }

    private fun onClick() {

    }
}