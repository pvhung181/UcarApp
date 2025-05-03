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
import com.pvhung.ucar.databinding.DialogViewRatingBinding

class ViewRatingDialog(
    context: Context
) : Dialog(context) {

    private val binding: DialogViewRatingBinding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.dialog_view_rating,
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

    fun setReview(rating: Int, review: String) {
        binding.ratingBar.rating = rating.toFloat()
        binding.etFeedback.setText(review)
    }

    private fun initDialog() {
        onCLick()
    }

    private fun onCLick() {
        binding.tvExit.setOnClickListener {
            dismiss()
        }

    }
}