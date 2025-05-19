package com.pvhung.ucar.ui.customer.customer_account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentCustomerAccountBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.main.MainActivity
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.LocaleUtils
import com.pvhung.ucar.utils.MethodUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.Utils

class CustomerAccountFragment :
    BaseBindingFragment<FragmentCustomerAccountBinding, CustomerAccountViewModel>() {
    private var db: DatabaseReference? = null
    private var dbListener: ValueEventListener? = null

    override fun getViewModel(): Class<CustomerAccountViewModel> {
        return CustomerAccountViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_account

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        getData()
        initData()
        initView()
        onClick()
    }

    private fun getData() {
        Utils.getUid {
            db = FirebaseDatabaseUtils.getCurrentCustomerDatabase()
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
            if (user.avatar != "") {
                try {
                    val bm = MethodUtils.base64ToBitmap(user.avatar)
                    binding.ivAvatar.setImageBitmap(bm)
                    binding.ivAvatar.setImageBitmap(bm)
                } catch (_: Exception) {
                }
            } else {
                binding.ivAvatar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.iv_male_avatar
                    )
                )

            }
        }
    }


    private fun initData() {

    }

    private fun initView() {
        if(mPrefHelper.getString(Constant.PREF_SETTING_LANGUAGE) == Constant.LANGUAGE_EN) {
            binding.tvCurrentSetLanguage.text = getString(R.string.english)
        }
        else {
            binding.tvCurrentSetLanguage.text = getString(R.string.vietnam)
        }
    }

    private fun onClick() {

        binding.tvInfo.setOnClickListener {
            navigateScreen(null, R.id.customerInfoFragment)
        }

        binding.tvLanguage.setOnClickListener {
            setLanguage()
        }

        binding.tvPolicy.setOnClickListener {
            navigateScreen(null, R.id.policyFragment2)
        }

//        binding.tvRate.setOnClickListener {
//            showRateApp()
//        }

        binding.tvFb.setOnClickListener {
            feedBack()
        }

        binding.tvShareApp.setOnClickListener {
            shareApp()
        }

        binding.tvLogout.setOnClickListener {
            signOut()
        }
    }

    private fun setLanguage() {
        if(LocaleUtils.codeLanguageCurrent == Constant.LANGUAGE_EN) {
            mPrefHelper.storeString(
                Constant.PREF_SETTING_LANGUAGE,
                Constant.LANGUAGE_VN
            )
            LocaleUtils.applyLocaleAndRestartCustomer(
                requireActivity(),
                Constant.LANGUAGE_VN
            )
        }
        else {
            mPrefHelper.storeString(
                Constant.PREF_SETTING_LANGUAGE,
                Constant.LANGUAGE_EN
            )
            LocaleUtils.applyLocaleAndRestartCustomer(
                requireActivity(),
                Constant.LANGUAGE_EN
            )
        }

    }


    override fun observerData() {

    }

    private fun shareApp() {
        val text =
            (getString(R.string.app_name) + requireContext().resources.getString(R.string.link_share_app)
                    + requireContext().packageName)
        MethodUtils.shareText(requireContext(), text, getString(R.string.app_name))
    }

    private fun feedBack() {
        MethodUtils.sendEmail(requireContext(), getString(R.string.emailFeedBack))
    }


//    private fun gotoPolicy() {
//        navigateSlideScreen(null, R.id.policyFragment)
//    }
//
//    private fun gotoLanguage() {
//        currentId = R.id.languageFragment
//        navigateScreen()
//    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        requireActivity().finish()
    }
}