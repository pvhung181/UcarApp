package com.pvhung.ucar.ui.main.signup

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.pvhung.ucar.R
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentSignUpBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.driver.DriverActivity
import com.pvhung.ucar.utils.DateUtils
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.Validator
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible
import com.pvhung.ucar.utils.showKeyboard
import java.util.Calendar


class SignUpFragment : BaseBindingFragment<FragmentSignUpBinding, SignUpViewModel>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: AuthStateListener
    private val datePickerDialog by lazy {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                binding.etDate.text = DateUtils.simpleFormatDate(dayOfMonth, month, year)
            },
            Calendar.getInstance()[Calendar.YEAR],
            Calendar.getInstance()[Calendar.MONTH],
            Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        )
    }

    override fun getViewModel(): Class<SignUpViewModel> {
        return SignUpViewModel::class.java
    }


    override val layoutId: Int
        get() = R.layout.fragment_sign_up

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initFirebase()
        initView()
        setOnClick()
        setOnTextChange()
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(firebaseAuthStateListener)
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()

        firebaseAuthStateListener = object : AuthStateListener {
            override fun onAuthStateChanged(fa: FirebaseAuth) {
                fa.currentUser?.let {
                    startActivity(Intent(requireActivity(), DriverActivity::class.java))
                    requireActivity().finish()
                    return
                }
            }
        }
    }

    private fun initView() {

    }

    private fun setOnClick() {
        binding.etGender.setOnClickListener {
            toggleSelectGenderDialog()
        }

        binding.bgGenderDialog.setOnClickListener { needToShowSelectGenderDialog(false) }

        binding.icSelectGender.tvMale.setOnClickListener {
            binding.etGender.text = getString(R.string.male)
            needToShowSelectGenderDialog(false)
        }
        binding.icSelectGender.tvFemale.setOnClickListener {
            binding.etGender.text = getString(R.string.female)
            needToShowSelectGenderDialog(false)
        }

        binding.tvSIgnIn.setOnClickListener {
            navigateScreen(null, R.id.signInFragment)
        }

        binding.viewBlock.setOnTouchListener { v, _ ->
            v.performClick()
            true
        }

        binding.etDate.setOnClickListener { datePickerDialog.show() }

        binding.signUpButton.setOnClickListener {

            if (checkClick() && isUserInfoValid()) {
                loading(true)
                mAuth.createUserWithEmailAndPassword(
                    binding.etEmail.text.toString(), binding.etPassword.text.toString()
                ).addOnSuccessListener {
                    val user = mAuth.currentUser
                    if (user != null) {
                        val userInfo = User(
                            fullName = binding.etFullName.text.toString(),
                            phoneNumber = binding.etPhone.text.toString(),
                            email = binding.etEmail.text.toString(),
                            gender = binding.etGender.text.toString(),
                            dateOfBirth = binding.etDate.text.toString(),
                            password = binding.etPassword.text.toString(),
                            isDriver = !binding.rbUser.isChecked
                        )
                        FirebaseDatabaseUtils.saveUserInfo(user.uid, userInfo)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Fail", Toast.LENGTH_SHORT).show()
                }.addOnCompleteListener {
                    loading(false)
                }

            }
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.viewBlock.beVisible()
            binding.signUpButton.beInvisible()
            binding.progress.beVisible()
        } else {
            binding.viewBlock.beGone()
            binding.signUpButton.beVisible()
            binding.progress.beGone()
        }
    }

    private fun setOnTextChange() {
        binding.etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvFullNameAlert.beGone()
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.tvConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    override fun observerData() {

    }

    //region utils
    private fun isUserInfoValid(): Boolean {
        if (!isRequiredInfoFilled()) return false
        if (!isPasswordValid()) return false
        if (!isEmailValid()) return false


        return true
    }

    private fun isDateOfBirthValid(): Boolean {


        return true
    }

    private fun isEmailValid(): Boolean {
        val email = binding.etEmail.text.toString()
        if (email.isBlank()) return true
        return Validator.isEmailValid(email)
    }

    private fun isRequiredInfoFilled(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.requestFocus()
            binding.etFullName.showKeyboard()
            binding.tvFullNameAlert.beVisible()
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.requestFocus()
            binding.etPassword.showKeyboard()
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.requestFocus()
            binding.etConfirmPassword.showKeyboard()
            return false
        }

        return true
    }

    private fun isPasswordValid(): Boolean {
        val password = binding.etFullName.text.toString()
        val confirmPassword = binding.etFullName.text.toString()

        return password == confirmPassword
    }
    //endregion

    private fun toggleSelectGenderDialog() {
        if (binding.bgGenderDialog.isVisible) needToShowSelectGenderDialog(false)
        else needToShowSelectGenderDialog(true)
    }

    private fun needToShowSelectGenderDialog(v: Boolean) {
        if (v) {
            binding.bgGenderDialog.beVisible()
            binding.icSelectGender.root.beVisible()
        } else {
            binding.bgGenderDialog.beGone()
            binding.icSelectGender.root.beGone()
        }
    }

}