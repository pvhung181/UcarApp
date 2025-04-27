package com.pvhung.ucar.ui.driver.driver_account

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.enums.SettingType
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentDriverAccountBinding
import com.pvhung.ucar.interfaces.OnSettingListener
import com.pvhung.ucar.ui.adapter.SettingsAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.main.MainActivity
import com.pvhung.ucar.utils.DataProvider
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.Utils

class DriverAccountFragment :
    BaseBindingFragment<FragmentDriverAccountBinding, DriverAccountViewModel>() {
    private lateinit var settingsAdapter: SettingsAdapter
    private var db: DatabaseReference? = null
    private var dbListener: ValueEventListener? = null


    override fun getViewModel(): Class<DriverAccountViewModel> {
        return DriverAccountViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_account

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        getData()
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

    }

    private fun onClick() {
        binding.ivEdit.setOnClickListener {
            navigateScreen(null, R.id.driverInfoFragment)
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

    private fun getData() {
        Utils.getUid {
            db = FirebaseDatabaseUtils.getCurrentDriverDatabase()
            dbListener?.let {
                db?.removeEventListener(it)
            }
            dbListener = db?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = FirebaseDatabaseUtils.getUserFromSnapshot(snapshot)
                        updateUi(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }

    private fun updateUi(user: User?) {
        user?.let {
            binding.tvPhone.text = user.phoneNumber
            binding.tvUserName.text = user.fullName
        }
    }


}