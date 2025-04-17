package com.pvhung.ucar.ui.customer.customer_map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import com.pvhung.ucar.common.enums.RideInfoState
import com.pvhung.ucar.databinding.FragmentCustomerMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.dialog.EnableGpsDialog
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beVisible

class CustomerMapFragment : BaseBindingFragment<FragmentCustomerMapBinding, CustomerMapViewModel>(),
    OnMapReadyCallback {

    private val enableGpsDialog by lazy { EnableGpsDialog(requireContext()) }
    private var isChangeUiWhenDriverFound = false
    private var rideState = RideInfoState.IDLE

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var pickupLocation: LatLng? = null
    private var radius: Double = 1.0
    private var isFoundDriver = false
    private var foundDriverId: String? = null
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

    }

    private fun initView() {

    }

    override fun observerData() {

    }

    private fun onClick() {
        binding.icNotifyMinimal.ivExpand.setOnClickListener {
            binding.icNotifyMinimal.root.beGone()
            binding.icNotifyExpand.root.beVisible()
        }
        binding.icNotifyExpand.ivCollapse.setOnClickListener {
            binding.icNotifyMinimal.root.beVisible()
            binding.icNotifyExpand.root.beGone()
        }

        binding.callUberBtn.setOnClickListener {
            when (rideState) {
                RideInfoState.IDLE -> {
                    requestUcar()
                }

                RideInfoState.LOOKING -> {
                    cancelRequest()
                }

                RideInfoState.RIDING -> {

                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionHelper.isGpsEnabled(requireContext()) && enableGpsDialog.isShowing) enableGpsDialog.dismiss()
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
                PermissionHelper.requestLocationPermission(locationPermissionLauncher)
            }
        }

    }

    private fun getClosestDriver() {
        binding.callUberBtn.setText("Looking for driver location...")
        rideState = RideInfoState.LOOKING
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
                    updateWhenFoundDriver()
                    if (map[0] != null) {
                        locationLat = (map[0].toString()).toDouble()
                    }
                    if (map[1] != null) {
                        locationLng = (map[1].toString()).toDouble()
                    }
                    var driverLocation = LatLng(locationLat, locationLng)
                    mDriverMarker?.remove()


                    val location1 = Location("").apply {
                        latitude = pickupLocation!!.latitude
                        longitude = pickupLocation!!.longitude
                    }

                    val location2 = Location("").apply {
                        latitude = driverLocation.latitude
                        longitude = driverLocation.longitude
                    }

                    val distance = location1.distanceTo(location2)

                    if (distance < 100) updateWhenDriverArrived()

                    mDriverMarker = mMap.addMarker(
                        MarkerOptions().position(driverLocation).title("Your driver")
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun updateWhenDriverArrived() {
        binding.icNotifyExpand.btnRide.text = getString(R.string.arrived)
        rideState = RideInfoState.RIDING
    }

    fun updateWhenFoundDriver() {
        if (!isChangeUiWhenDriverFound) {
            binding.callUberBtn.beGone()
            binding.icNotifyMinimal.root.beVisible()
            binding.icNotifyExpand.btnRide.text = getString(R.string.cancel)
            isChangeUiWhenDriverFound = true
        }

    }

    fun updateWhenRideDone() {
        binding.callUberBtn.text = getString(R.string.call_ucar)
        binding.callUberBtn.beVisible()
        binding.icNotifyMinimal.root.beGone()
        binding.icNotifyExpand.root.beGone()
        isChangeUiWhenDriverFound = false
        rideState = RideInfoState.IDLE
    }

    private fun requestUcar() {
        if (checkClick()) {
            if (!PermissionHelper.hasLocationPermission(requireContext())) {
                PermissionHelper.requestLocationPermission(locationPermissionLauncher)
                return
            }
            if (!PermissionHelper.isGpsEnabled(requireContext())) {
                enableGpsDialog.show()
                return
            }

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

    private fun cancelRequest() {
        rideState = RideInfoState.IDLE
//        updateWhenRideDone()
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            val ref = FirebaseDatabaseUtils.getRequestsDatabase().child(it)
            ref.removeValue()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {


        } catch (_: Exception) {
        }
    }
}