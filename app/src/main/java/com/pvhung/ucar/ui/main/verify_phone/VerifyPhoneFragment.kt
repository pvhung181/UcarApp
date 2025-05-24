package com.pvhung.ucar.ui.main.verify_phone

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentForgetPasswordBinding
import com.pvhung.ucar.databinding.FragmentVerifyPhoneNumberBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.main.signin.SignInViewModel
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible
import java.util.concurrent.TimeUnit

class VerifyPhoneFragment :
    BaseBindingFragment<FragmentVerifyPhoneNumberBinding, SignInViewModel>() {
    private lateinit var mAuth: FirebaseAuth
    private var user = User()


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.e("TAG", "onCodeSent: ", )


        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("TAG", "onCodeSent: ", )

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.e("TAG", "onCodeSent 23: ${verificationId}", )
        }
    }


    override fun getViewModel(): Class<SignInViewModel> {
        return SignInViewModel::class.java
    }


    override val layoutId: Int
        get() = R.layout.fragment_verify_phone_number

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initData()
        initFirebase()
        initView()
        setOnClick()
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
    }

    private fun initData() {
        arguments?.let {
            user = User(
                fullName = it.getString(Constant.USER_NAME) ?: "",
                phoneNumber = it.getString(Constant.USER_PHONE) ?: "",
                email = it.getString(Constant.USER_EMAIL) ?: "",
                gender = it.getString(Constant.USER_GENDER) ?: "",
                dateOfBirth = it.getString(Constant.USER_BIRTH) ?: "",
                password = it.getString("password") ?: "",
                isDriver = it.getBoolean(Constant.USER_IS_DRIVER, false),
                numberPlate = it.getString(Constant.USER_NUMBER_PLATE) ?: "",
            )

            binding.phoneEditTextNumber.setText(user.phoneNumber)

            user.setService(it.getString(Constant.USER_SERVICE) ?:"")
//            FirebaseDatabaseUtils.saveUserInfo(mAuth.currentUser?.uid!!, user)
        }
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+84" + user.phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun initView() {


    }

    private fun setOnClick() {

    }

    override fun observerData() {

    }

    override fun needInsetTop(): Boolean {
        return true
    }
}