package com.pvhung.ucar.ui.customer.customer_notification

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentCustomerNotificationBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class CustomerNotificationFragment :
    BaseBindingFragment<FragmentCustomerNotificationBinding, CustomerNotificationViewModel>() {

    override fun getViewModel(): Class<CustomerNotificationViewModel> {
        return CustomerNotificationViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_notification

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