package com.pvhung.ucar.ui.customer.customer_map

import androidx.lifecycle.MutableLiveData
import com.pvhung.ucar.common.enums.UserBookingState
import com.pvhung.ucar.ui.base.BaseViewModel

class CustomerMapViewModel : BaseViewModel() {
    val bookState = MutableLiveData<UserBookingState>(UserBookingState.IDLE)
}