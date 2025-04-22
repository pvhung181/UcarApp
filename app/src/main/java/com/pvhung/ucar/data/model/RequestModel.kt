package com.pvhung.ucar.data.model

import com.pvhung.ucar.common.enums.RequestState

data class RequestModel(
    var customerId: String = "",
    var state: RequestState = RequestState.IDLE,
    var destination: String = ""
) {
    fun reset() {
        customerId = ""
        state = RequestState.IDLE
        destination = ""
    }
}