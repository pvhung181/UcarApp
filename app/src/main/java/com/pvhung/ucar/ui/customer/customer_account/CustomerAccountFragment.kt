package com.pvhung.ucar.ui.customer.customer_account

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.pvhung.ucar.R
import com.pvhung.ucar.common.enums.SettingType
import com.pvhung.ucar.databinding.FragmentCustomerAccountBinding
import com.pvhung.ucar.interfaces.OnSettingListener
import com.pvhung.ucar.ui.adapter.SettingsAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.main.MainActivity
import com.pvhung.ucar.utils.DataProvider
import com.pvhung.ucar.utils.OnBackPressed

class CustomerAccountFragment :
    BaseBindingFragment<FragmentCustomerAccountBinding, CustomerAccountViewModel>() {
    private lateinit var settingsAdapter: SettingsAdapter

    override fun getViewModel(): Class<CustomerAccountViewModel> {
        return CustomerAccountViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_account

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        initData()
        initView()
        onClick()
    }

    private fun initData() {
        settingsAdapter = SettingsAdapter(
            requireContext(),
            DataProvider.getSettingItems(),
            object : OnSettingListener {
                override fun onItemClick(type: SettingType) {
                    when (type) {
                        SettingType.SIGN_OUT -> {
                            signOut()
                        }
                    }
                }

            }
        )
    }

    private fun initView() {
        binding.rvSetting.adapter = settingsAdapter
    }

    private fun onClick() {
        binding.ivEdit.setOnClickListener {
            navigateScreen(null, R.id.customerInfoFragment)
        }
    }

    override fun observerData() {

    }


    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        requireActivity().finish()
    }
}