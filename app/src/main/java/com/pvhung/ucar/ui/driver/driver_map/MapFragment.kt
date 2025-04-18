package com.pvhung.ucar.ui.driver.driver_map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.databinding.FragmentDriverMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper

class MapFragment : BaseBindingFragment<FragmentDriverMapBinding, MapViewModel>(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var customerId = ""
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {

        } else {

        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (locationResult != null) {
                locationResult.lastLocation?.let {
                    mLastLocation = it
                    val newPos = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))



                    FirebaseAuth.getInstance().currentUser?.uid?.let { id ->
                        val refAvailable = FirebaseDatabase.getInstance()
                            .getReference(Constant.DRIVERS_AVAILABLE_REFERENCES)
                        val refWorking = FirebaseDatabase.getInstance()
                            .getReference(Constant.DRIVERS_WORKING_REFERENCES)

                        val geoAvailable = GeoFire(refAvailable)
                        val geoWorking = GeoFire(refWorking)

                        when (customerId) {
                            "" -> {
                                geoWorking.removeLocation(id)
                                geoAvailable.setLocation(id, GeoLocation(it.latitude, it.longitude))
                            }

                            else -> {
                                geoAvailable.removeLocation(id)
                                geoWorking.setLocation(id, GeoLocation(it.latitude, it.longitude))
                            }
                        }
                    }

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
        getAssignedCustomer()
    }

    private fun getAssignedCustomer() {
        val driverId = FirebaseAuth.getInstance().currentUser?.uid!!
        val assignedCustomerRef = FirebaseDatabaseUtils.getSpecificRiderDatabase(driverId)
            .child(Constant.CUSTOMER_RIDE_ID)

        assignedCustomerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    customerId = snapshot.value.toString();
                    getAssignedCustomerPickupLocation()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getAssignedCustomerPickupLocation() {
        val assignedCustomerPickupRef = FirebaseDatabaseUtils.getRequestsDatabase()
            .child(customerId).child("l")

        assignedCustomerPickupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val map = snapshot.value as List<*>
                    var locationLat = 0.0
                    var locationLng = 0.0
                    if (map[0] != null) {
                        locationLat = (map[0].toString()).toDouble()
                    }
                    if (map[1] != null) {
                        locationLng = (map[1].toString()).toDouble()
                    }
                    var driverLocation = LatLng(locationLat, locationLng)
                    mMap.addMarker(
                        MarkerOptions().position(driverLocation).title("Pickup location")
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun initMaps() {
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    private fun init() {

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
                PermissionHelper.requestLocationPermission(locationPermissionLauncher)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            FirebaseAuth.getInstance().currentUser?.uid?.let { id ->
                val ref = FirebaseDatabase.getInstance()
                    .getReference(Constant.DRIVERS_AVAILABLE_REFERENCES)
                val geo = GeoFire(ref)
                geo.removeLocation(id)
            }

        } catch (_: Exception) {
        }
    }

}