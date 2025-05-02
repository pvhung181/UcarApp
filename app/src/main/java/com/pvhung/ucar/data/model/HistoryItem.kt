package com.pvhung.ucar.data.model

data class HistoryItem(
    var id: String = "",
    var customerId: String = "",
    var driverId: String = "",
    var destination: String = "",
    var pickupLocation: String = "",
    var rating: Double = 0.0,
    var review: String = "",
    var money: Double = 0.0,
    var distance: Double = 0.0,
    var time: Long = 0
)