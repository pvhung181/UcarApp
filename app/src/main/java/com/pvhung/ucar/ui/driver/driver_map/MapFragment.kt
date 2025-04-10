package com.pvhung.ucar.ui.driver.driver_map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
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
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentDriverMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper

//todo fix uid null

class MapFragment : BaseBindingFragment<FragmentDriverMapBinding, MapViewModel>(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
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


    override fun getViewModel(): Class<MapViewModel> {
        return MapViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_riders_map

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
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        } catch (_: Exception) {
        }
    }

}