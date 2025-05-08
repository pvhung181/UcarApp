package com.pvhung.ucar.ui.customer.customer_map

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pvhung.ucar.common.enums.UserBookingState
import com.pvhung.ucar.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CustomerMapViewModel : BaseViewModel() {
    fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        callback: (String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                val address = addresses?.firstOrNull()
                val fullAddress = address?.getAddressLine(0)

                withContext(Dispatchers.Main) {
                    callback(fullAddress)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
}