package com.pvhung.ucar.ui.driver.driver_notification

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentDriverNotificationBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class DriverNotificationFragment :
    BaseBindingFragment<FragmentDriverNotificationBinding, DriverNotificationViewModel>() {

    override fun getViewModel(): Class<DriverNotificationViewModel> {
        return DriverNotificationViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_notification

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

    private fun onClick() {

    }

    override fun observerData() {

    }
}