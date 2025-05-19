package com.pvhung.ucar.ui.driver.driver_info

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.App
import com.pvhung.ucar.R
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentDriverInfoBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.MethodUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.Utils
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible

class DriverInfoFragment : BaseBindingFragment<FragmentDriverInfoBinding, DriverInfoViewModel>() {
    private var db: DatabaseReference? = null
    private var dbListener: ValueEventListener? = null
    private var resultUri: Uri? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                resultUri = uri
                binding.ivAvatar.setImageURI(resultUri)
            }
        }

    override fun getViewModel(): Class<DriverInfoViewModel> {
        return DriverInfoViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_info

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        getData()
        initView()
        onClick()
    }


    private fun initView() {

    }

    private fun onClick() {
        binding.icHeader.backButton.setOnClickListener { popBackStack() }

        binding.saveBtn.setOnClickListener {

            val map = mutableMapOf<String, Any>(
                "fullName" to binding.etFullName.text.toString(),
                "phoneNumber" to binding.etPhone.text.toString(),
                "email" to binding.etEmail.text.toString(),
                "gender" to binding.etGender.text.toString(),
                "dateOfBirth" to binding.etDate.text.toString()

            )
            if(resultUri != null) {
                val bm = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, resultUri)
                map.put("avatar", MethodUtils.bitmapToBase64(bm))
            }
            binding.savingProgress.beVisible()
            binding.saveBtn.beInvisible()
            FirebaseDatabaseUtils.getCurrentDriverDatabase().updateChildren(map)
                .addOnSuccessListener {
                    showToast("Update successfully")
                }
                .addOnCompleteListener {
                    binding.saveBtn.beVisible()
                    binding.savingProgress.beGone()
                }

        }

        binding.ivChooseAvatar.setOnClickListener {
            if (checkClick()) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    override fun observerData() {

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
                        binding.progress.beGone()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }


    private fun updateUi(user: User?) {
        user?.let {
            binding.etFullName.setText(user.fullName)
            binding.etPhone.setText(user.phoneNumber)
            binding.etEmail.setText(user.email)
            binding.etGender.setText(user.gender)
            binding.etDate.setText(user.dateOfBirth)

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


    override fun needInsetTop(): Boolean {
        return true
    }

}