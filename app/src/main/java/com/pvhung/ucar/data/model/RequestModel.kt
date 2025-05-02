package com.pvhung.ucar.data.model

import com.pvhung.ucar.common.enums.RequestState

data class RequestModel(
    var customerId: String = "",
    var state: RequestState = RequestState.IDLE,
    var destination: String = "",
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var pickupLocation: String = ""
) {
    fun reset() {
        customerId = ""
        state = RequestState.IDLE
        destination = ""
        destinationLng = 0.0
        destinationLat = 0.0
    }
}