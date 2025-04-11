package com.pvhung.ucar.ui.driver.driver_account

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentDriverAccountBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class DriverAccountFragment :
    BaseBindingFragment<FragmentDriverAccountBinding, DriverAccountViewModel>() {

    override fun getViewModel(): Class<DriverAccountViewModel> {
        return DriverAccountViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_account

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