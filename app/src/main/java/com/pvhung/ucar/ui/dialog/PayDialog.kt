package com.pvhung.ucar.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.DialogPayFeeBinding

class PayDialog(
    context: Context
) : Dialog(context) {

    private val binding: DialogPayFeeBinding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.dialog_pay_fee,
            null,
            false
        )
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    private fun initDialog() {
        onCLick()
    }

    fun setMoney(s: String) {
        binding.tvMoney.text = s
        binding.tvDes.text = context.getString(R.string.you_will_receive_for_this_trip, s)
    }

    private fun onCLick() {
        binding.btnReceived.setOnClickListener {
            dismiss()
        }

        binding.root.setOnClickListener { dismiss() }

    }
}