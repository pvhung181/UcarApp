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
import com.pvhung.ucar.databinding.DialogSearchingDriverBinding

class SearchingDriverDialog(context: Context, val listener: ItemClickListener) : Dialog(context) {

    private val binding: DialogSearchingDriverBinding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.dialog_searching_driver,
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

    private fun onCLick() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            listener.onCancelClick()
        }
    }

    interface ItemClickListener {
        fun onCancelClick()
    }
}