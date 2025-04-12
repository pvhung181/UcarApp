package com.pvhung.ucar.ui.customer.customer_activity

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentCustomerActivityBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class CustomerActivityFragment :
    BaseBindingFragment<FragmentCustomerActivityBinding, CustomerActivityViewModel>() {

    override fun getViewModel(): Class<CustomerActivityViewModel> {
        return CustomerActivityViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_activity

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