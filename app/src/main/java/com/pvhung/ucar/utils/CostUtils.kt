package com.pvhung.ucar.utils

import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.roundToInt

object CostUtils {
    fun getCost(distanceInMeters: Float): Float {
        val baseFareVND = 10000f
        val baseDistance = 2000f
        val costPerKmVND = 4000f

        val totalVND = if (distanceInMeters <= baseDistance) {
            baseFareVND
        } else {
            val extraDistance = distanceInMeters - baseDistance
            val extraKm = ceil(extraDistance / 1000).toInt()
            baseFareVND + extraKm * costPerKmVND
        }
        return totalVND
    }

    fun formatCurrency(amount: Float): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount.toLong()) + " VND"
    }

    fun getCarCost(distanceInMeters: Float): Float {
        val earn = getCost(distanceInMeters) * 1.5
        return ((earn * 100).roundToInt()) / 100f
    }
}