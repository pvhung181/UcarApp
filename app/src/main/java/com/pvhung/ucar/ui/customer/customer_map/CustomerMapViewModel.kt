package com.pvhung.ucar.ui.customer.customer_map

import androidx.lifecycle.MutableLiveData
import com.pvhung.ucar.common.enums.UserBookState
import com.pvhung.ucar.ui.base.BaseViewModel

class CustomerMapViewModel : BaseViewModel() {
    val bookState = MutableLiveData<UserBookState>(UserBookState.IDLE)
}