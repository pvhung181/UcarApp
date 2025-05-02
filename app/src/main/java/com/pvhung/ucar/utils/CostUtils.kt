package com.pvhung.ucar.utils

import kotlin.math.ceil
import kotlin.math.roundToInt

object CostUtils {
    fun getCost(distanceInMeters: Float): Float {
        val baseFareVND = 10000f
        val baseDistance = 2000f
        val costPerKmVND = 4000f
        val exchangeRate = 24000f

        val totalVND = if (distanceInMeters <= baseDistance) {
            baseFareVND
        } else {
            val extraDistance = distanceInMeters - baseDistance
            val extraKm = ceil(extraDistance / 1000).toInt()
            baseFareVND + extraKm * costPerKmVND
        }

        val totalUSD = totalVND / exchangeRate
        return ((totalUSD * 100).roundToInt()) / 100f
    }

    fun getCarCost(distanceInMeters: Float): Float {
        val earn = getCost(distanceInMeters) * 1.5
        return ((earn * 100).roundToInt()) / 100f
    }
}