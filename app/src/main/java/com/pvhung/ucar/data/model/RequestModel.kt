package com.pvhung.ucar.data.model

import com.pvhung.ucar.common.enums.RequestState
import kotlin.math.cos

data class RequestModel(
    var customerId: String = "",
    var state: RequestState = RequestState.IDLE,
    var destination: String = "",
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var pickupLocation: String = "",
    var time: Long = 0,
    var distance: Double = 0.0,
    var cost: Float = 0f
) {
    fun reset() {
        customerId = ""
        state = RequestState.IDLE
        destination = ""
        destinationLng = 0.0
        destinationLat = 0.0
        time = 0
        distance = 0.0
        cost = 0f
    }
}