package com.pvhung.ucar.data.model

import com.pvhung.ucar.common.enums.RequestState

data class RequestModel(
    var customerId: String = "",
    var state: RequestState = RequestState.IDLE
) {
    fun reset() {
        customerId = ""
        state = RequestState.IDLE
    }
}