package com.pvhung.ucar.ui.customer.customer_map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentCustomerMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper

class CustomerMapFragment : BaseBindingFragment<FragmentCustomerMapBinding, CustomerMapViewModel>(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var pickupLocation: LatLng? = null
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (locationResult != null) {
                locationResult.lastLocation?.let {
                    mLastLocation = it
                    val newPos = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
                }
            }
        }
    }


    override fun getViewModel(): Class<CustomerMapViewModel> {
        return CustomerMapViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_customer_map

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        loading(true)
        initMaps()
        init()
        initView()
        onClick()
    }

    private fun initMaps() {
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    private fun init() {
        if (!PermissionHelper.hasLocationPermission(requireContext())) {
            PermissionHelper.requestLocationPermission(requireActivity())
        }
    }

    private fun initView() {

    }

    override fun observerData() {

    }

    private fun onClick() {
        binding.callUberBtn.setOnClickListener {
            if (checkClick()) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid!!
                val ref = FirebaseDatabaseUtils.getRequestsDatabase()
                val geo = GeoFire(ref)
                mLastLocation?.let { ll ->
                    geo.setLocation(uid, GeoLocation(ll.latitude, ll.longitude))
                    pickupLocation = LatLng(ll.latitude, ll.longitude)
                    mMap.addMarker(MarkerOptions().position(pickupLocation!!).title("Pick up here"))

                    //todo show progressing
                }
            }

        }
    }


    private fun loading(isLoading: Boolean) {
//        if (isLoading) {
//            binding.viewBlock.beVisible()
//            binding.progress.beVisible()
//        } else {
//            binding.viewBlock.beGone()
//            binding.progress.beGone()
//        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(gm: GoogleMap) {
        loading(false)
        mMap = gm
        mMap.uiSettings.isZoomControlsEnabled = true

        mLocationRequest = LocationRequest().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        if (DeviceHelper.isMinSdk23) {
            if (PermissionHelper.hasLocationPermission(requireActivity())) {
                mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )
                mMap.isMyLocationEnabled = true
            } else {
                PermissionHelper.requestLocationPermission(requireActivity())
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        try {


        } catch (_: Exception) {
        }
    }
}