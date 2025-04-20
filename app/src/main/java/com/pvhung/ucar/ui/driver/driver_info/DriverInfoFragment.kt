package com.pvhung.ucar.ui.driver.driver_info

import android.os.Bundle
import android.view.View
import com.pvhung.ucar.R
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentDriverInfoBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.beGone

class DriverInfoFragment : BaseBindingFragment<FragmentDriverInfoBinding, DriverInfoViewModel>() {
    override fun getViewModel(): Class<DriverInfoViewModel> {
        return DriverInfoViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_info

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        initView()
        onClick()
    }


    private fun initView() {

    }

    private fun onClick() {
        binding.icHeader.backButton.setOnClickListener { popBackStack() }
    }

    override fun observerData() {
        mainViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progress.beGone()
                updateUi(it)
            }
        }
    }

    private fun updateUi(user: User) {
        binding.etFullName.setText(user.fullName)
        binding.etPhone.setText(user.phoneNumber)
        binding.etEmail.setText(user.email)
        binding.etGender.setText(user.gender)
        binding.etDate.setText(user.dateOfBirth)
    }

    override fun needInsetTop(): Boolean {
        return true
    }

}