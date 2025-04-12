package com.pvhung.ucar.ui.customer.customer_map

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentCustomerMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class CustomerMapFragment :
    BaseBindingFragment<FragmentCustomerMapBinding, CustomerMapViewModel>() {

    override fun getViewModel(): Class<CustomerMapViewModel> {
        return CustomerMapViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_map

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