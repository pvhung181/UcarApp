package com.pvhung.ucar.ui.dialog

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.databinding.BottomSheetBookingVehicleBinding
import java.util.Locale

class BottomSheetBookingVehicle(context: Context, val listener: OnRequestListener) :
    BottomSheetDialog(context) {

    private val binding: BottomSheetBookingVehicleBinding
    var currentVehicle = Constant.MOTOBIKE_SERVICE
    var motobikeCost: Float = 0f
    var carCost: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!
            .setLayout(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.bottom_sheet_booking_vehicle,
            null,
            false
        )

        setContentView(binding.root)

        setOnDismissListener {
            listener.onDismiss()
        }

        binding.bookingBtn.setOnClickListener {
            listener.onBook(currentVehicle)
            this.dismiss()
        }

        binding.bgCar.setOnClickListener {
            currentVehicle = Constant.CAR_SERVICE
            binding.bgCar.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_F1F4FF))
            binding.bgMotobike.backgroundTintList = null
        }

        binding.bgMotobike.setOnClickListener {
            currentVehicle = Constant.MOTOBIKE_SERVICE
            binding.bgMotobike.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_F1F4FF))
            binding.bgCar.backgroundTintList = null

        }
    }

    fun setCost(cost: Float, carCost: Float) {
        motobikeCost = cost
        this.carCost = carCost
        binding.tvPrice.text = String.format(Locale.US, "$%.2f", cost)
        binding.tvPriceCar.text = String.format(Locale.US, "$%.2f", carCost)
    }

    fun getCost(): Float {
        if (currentVehicle == Constant.MOTOBIKE_SERVICE) {
            return motobikeCost
        } else {
            return carCost
        }
    }

    interface OnRequestListener {
        fun onBook(service: String)
        fun onDismiss()
    }
}