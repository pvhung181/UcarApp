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
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.DialogRatingRideBinding
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible

class RatingRideDialog(
    context: Context,
    val listener: RatingListener
) : Dialog(context) {

    private val binding: DialogRatingRideBinding
    private var historyItem = HistoryItem()

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.dialog_rating_ride,
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
        binding.tvSubmit.setOnClickListener {
            listener.onSubmit(
                historyItem,
                binding.ratingBar.rating.toInt(),
                binding.etFeedback.text.toString()
            )
        }

        binding.tvCancel.setOnClickListener {
            listener.onCancel()
            dismiss()
        }
    }

    fun setHistoryItem(historyItem: HistoryItem) {
        this.historyItem = historyItem
    }

    fun loading(v: Boolean) {
        if (v) {
            binding.progress.beVisible()
            binding.tvSubmit.beInvisible()
            binding.ratingBar.beInvisible()
            binding.etFeedback.beInvisible()
        } else {
            binding.progress.beInvisible()
            binding.tvSubmit.beVisible()
            binding.ratingBar.beVisible()
            binding.etFeedback.beInvisible()
        }
    }

    interface RatingListener {
        fun onSubmit(historyItem: HistoryItem, rating: Int, review: String)
        fun onCancel()
    }
}