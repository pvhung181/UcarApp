package com.pvhung.ucar.ui.main.signin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentSignInBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.driver.DriverActivity
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible
import com.pvhung.ucar.utils.showKeyboard

class SignInFragment : BaseBindingFragment<FragmentSignInBinding, SignInViewModel>() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: AuthStateListener

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {

        }
    }

    fun requestNotificationPermissionIfNeeded() {
        if (!PermissionHelper.hasNotificationPermission(requireContext()) && DeviceHelper.isMinSdk33) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun getViewModel(): Class<SignInViewModel> {
        return SignInViewModel::class.java
    }


    override val layoutId: Int
        get() = R.layout.fragment_sign_in

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initFirebase()
        initView()
        setOnClick()
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        requestNotificationPermissionIfNeeded()
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()

        firebaseAuthStateListener = object : AuthStateListener {
            override fun onAuthStateChanged(fa: FirebaseAuth) {
                fa.currentUser?.let {
                    startActivity(Intent(requireActivity(), DriverActivity::class.java).apply {Intent.FLAG_ACTIVITY_SINGLE_TOP})
                    requireActivity().finish()
                    return
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(firebaseAuthStateListener)
    }

    private fun initView() {


    }

    private fun setOnClick() {
        binding.viewBlock.setOnTouchListener { v, _ ->
            v.performClick()
            true
        }

        binding.signInButton.setOnClickListener {
            if (checkClick() && isRequiredInfoFilled()) {
                signIn()
            }
        }

        binding.tvNewAccount.setOnClickListener {
            navigateScreen(null, R.id.signUpFragment)
        }
    }

    override fun observerData() {

    }

    private fun signIn() {
        loading(true)
        mAuth.signInWithEmailAndPassword(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        )
            .addOnSuccessListener {
                val user = mAuth.currentUser
                if(user != null) {
                    startActivity(Intent(requireActivity(), DriverActivity::class.java).apply {Intent.FLAG_ACTIVITY_SINGLE_TOP})
                    requireActivity().finish()
                }
            }
            .addOnFailureListener {

            }
            .addOnCompleteListener {
                loading(false)
            }

    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.viewBlock.beVisible()
            binding.signInButton.beInvisible()
            binding.progress.beVisible()
        } else {
            binding.viewBlock.beGone()
            binding.signInButton.beVisible()
            binding.progress.beGone()
        }
    }

    private fun isRequiredInfoFilled(): Boolean {
        val password = binding.etPassword.text.toString().trim()
        val account = binding.etEmail.text.toString().trim()

        if (account.isEmpty()) {
            showToast("Must be fill")
            binding.etEmail.requestFocus()
            binding.etEmail.showKeyboard()
            return false
        }

        if (password.isEmpty()) {
            showToast("Must be fill")
            binding.etPassword.requestFocus()
            binding.etPassword.showKeyboard()
            return false
        }

        return true
    }
}