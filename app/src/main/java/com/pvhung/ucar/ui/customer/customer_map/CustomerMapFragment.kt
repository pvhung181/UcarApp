package com.pvhung.ucar.ui.customer.customer_map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
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
    private var radius: Double = 1.0
    private var isFoundDriver = false
    private var foundDriverId: String? = null
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
                    getClosestDriver()
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
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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

    private fun getClosestDriver() {
        val ref = FirebaseDatabaseUtils.getDriverAvailableDatabase()
        val geo = GeoFire(ref)
        pickupLocation?.let {
            val geoQuery = geo.queryAtLocation(GeoLocation(it.latitude, it.longitude), radius)
            geoQuery.removeAllListeners()
            geoQuery.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
                override fun onDataEntered(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                    if (!isFoundDriver && dataSnapshot?.key != null) {
                        isFoundDriver = true
                        foundDriverId = dataSnapshot.key

                        val driverRef =
                            FirebaseDatabaseUtils.getSpecificRiderDatabase(foundDriverId!!)
                        val customerId = FirebaseAuth.getInstance().currentUser?.uid!!
                        val hashMap = mutableMapOf<String, Any>()
                        hashMap.put(Constant.CUSTOMER_RIDE_ID, customerId)
                        driverRef.updateChildren(hashMap)

                        binding.callUberBtn.setText("Looking for driver location...")
                        getDriverLocation()

                    }
                }

                override fun onDataExited(dataSnapshot: DataSnapshot?) {

                }

                override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onGeoQueryReady() {
                    if (!isFoundDriver) {
                        radius++
                        getClosestDriver()
                    } else {

                    }
                }

                override fun onGeoQueryError(error: DatabaseError?) {

                }

            })
        }
    }

    private var mDriverMarker: Marker? = null
    private fun getDriverLocation() {
        val driverLocationRef =
            FirebaseDatabaseUtils.getDriverWorkingDatabase().child(foundDriverId!!).child("l")
        driverLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val map = snapshot.value as List<*>
                    var locationLat = 0.0
                    var locationLng = 0.0
                    binding.callUberBtn.setText("Driver found")
                    if (map[0] != null) {
                        locationLat = (map[0].toString()).toDouble()
                    }
                    if (map[1] != null) {
                        locationLng = (map[1].toString()).toDouble()
                    }
                    var driverLocation = LatLng(locationLat, locationLng)
                    mDriverMarker?.let { it.remove() }


                    val location1 = Location("").apply {
                        latitude = pickupLocation!!.latitude
                        longitude = pickupLocation!!.longitude
                    }

                    val location2 = Location("").apply {
                        latitude = driverLocation.latitude
                        longitude = driverLocation.longitude
                    }

                    val distance = location1.distanceTo(location2)

                    showToast("Driver found ${distance} ")

                    mDriverMarker = mMap.addMarker(
                        MarkerOptions().position(driverLocation).title("Your driver")
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {


        } catch (_: Exception) {
        }
    }
}