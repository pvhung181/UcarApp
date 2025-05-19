package com.pvhung.ucar.ui.main.forget

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentForgetPasswordBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.main.signin.SignInViewModel
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible

class ForgetPasswordFragment :
    BaseBindingFragment<FragmentForgetPasswordBinding, SignInViewModel>() {
    private lateinit var mAuth: FirebaseAuth


    override fun getViewModel(): Class<SignInViewModel> {
        return SignInViewModel::class.java
    }


    override val layoutId: Int
        get() = R.layout.fragment_forget_password

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initFirebase()
        initView()
        setOnClick()
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()

    }

    private fun initView() {


    }

    private fun setOnClick() {
        binding.resetBtn.setOnClickListener {
            if (isValid()) {
                loading(true)
                mAuth.sendPasswordResetEmail(binding.etEmail.text.toString())
                    .addOnSuccessListener {
                        loading(false)
                        binding.tvWelcome.text = getString(R.string.email_sent_successfully)
                        binding.tvSubtitle.beGone()
                        binding.etEmail.beGone()
                        binding.resetBtn.text = getString(R.string.done)
                        binding.resetBtn.setOnClickListener {
                            popBackStack()
                        }
                    }
                    .addOnFailureListener {
                        showToast("Fail")
                    }
            }
        }

        binding.ivBack.setOnClickListener { popBackStack() }
    }

    private fun isValid(): Boolean {
        return (binding.etEmail.text.toString().isNotBlank())
    }

    override fun observerData() {

    }

    override fun needInsetTop(): Boolean {
        return true
    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.viewBlock.beVisible()
            binding.resetBtn.beInvisible()
            binding.progress.beVisible()
        } else {
            binding.viewBlock.beGone()
            binding.resetBtn.beVisible()
            binding.progress.beGone()
        }
    }

}