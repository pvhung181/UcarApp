package com.pvhung.ucar.ui.customer.customer_map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.common.enums.RequestState
import com.pvhung.ucar.common.enums.UserBookState
import com.pvhung.ucar.data.model.RequestModel
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
    private var bookState = UserBookState.IDLE
    private var isWaitingResponse = false

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var pickupLocation: LatLng? = null
    private var radius: Double = 1.0
    private var isFoundDriver = false
    private var foundDriverId: String? = null

    //Use for looking driver
    private var geoQuery: GeoQuery? = null
    private var driverLocationRef: DatabaseReference? = null
    private var driverLocationRefListener: ValueEventListener? = null

    private var pickupMarker: Marker? = null

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
            when (bookState) {
                UserBookState.IDLE -> {
                    requestUcar()
                }

                else -> {
                    cancelRequest()
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
        bookState = UserBookState.LOOKING
        val ref = FirebaseDatabaseUtils.getDriverAvailableDatabase()
        val geo = GeoFire(ref)
        pickupLocation?.let {
            geoQuery = geo.queryAtLocation(GeoLocation(it.latitude, it.longitude), radius)
            geoQuery?.removeAllListeners()
            geoQuery?.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
                override fun onDataEntered(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                    if (!isFoundDriver && dataSnapshot?.key != null && bookState == UserBookState.LOOKING) {
                        isFoundDriver = true
                        foundDriverId = dataSnapshot.key

                        val driverRef =
                            FirebaseDatabaseUtils.getSpecificRiderDatabase(foundDriverId!!)
                                .child(Constant.REQUEST_STATE_REFERENCES)
                        val customerId = FirebaseAuth.getInstance().currentUser?.uid!!
                        driverRef.setValue(RequestModel(customerId))
                        bookState = UserBookState.PENDING
                        if (!isWaitingResponse) {
                            isWaitingResponse = true
                            driverRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val request = snapshot.getValue(RequestModel::class.java)
                                        Log.d("pvhung1", "onDataChange: ${request}")
                                        if (request != null) {
                                            if (request.state == RequestState.ACCEPT) {
                                                showToast("Accepted")
                                                getDriverLocation()
                                                isWaitingResponse = false
                                                bookState = UserBookState.FOUND
                                            } else if (request.state == RequestState.CANCEL) {
                                                driverRef.removeValue()
                                                isFoundDriver = false
                                                foundDriverId = ""
                                                driverRef.removeEventListener(this)
                                                bookState = UserBookState.LOOKING
                                                showToast("Driver cancel")
                                            }

                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}

                            })
                        }
                    }
                }

                override fun onDataExited(dataSnapshot: DataSnapshot?) {

                }

                override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onGeoQueryReady() {
                    if (!isFoundDriver && bookState == UserBookState.LOOKING) {
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
        driverLocationRef =
            FirebaseDatabaseUtils.getDriverWorkingDatabase().child(foundDriverId!!).child("l")
        driverLocationRefListener =
            driverLocationRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("pvhung1", "onDataChange: getDriverLocation")
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
        bookState = UserBookState.RIDING
    }

    fun updateWhenFoundDriver() {
        if (!isChangeUiWhenDriverFound) {
            binding.callUberBtn.beGone()
            binding.icNotifyMinimal.root.beVisible()
            binding.icNotifyExpand.btnRide.text = getString(R.string.cancel)
            isChangeUiWhenDriverFound = true
        }

    }

    private fun updateWhenRideDone() {
        updateUiWhenRideDone()
        isChangeUiWhenDriverFound = false
        geoQuery?.removeAllListeners()
        driverLocationRefListener?.let {
            driverLocationRef?.removeEventListener(it)
        }
        radius = 1.0
        foundDriverId?.let {

            isFoundDriver = false
            foundDriverId = null
        }
        pickupMarker?.remove()
        bookState = UserBookState.IDLE

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
                pickupMarker =
                    mMap.addMarker(MarkerOptions().position(pickupLocation!!).title("Pick up here"))
                getClosestDriver()
                //todo show progressing
            }
        }
    }

    private fun cancelRequest() {
        bookState = UserBookState.IDLE
        updateWhenRideDone()
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

    //region update ui
    private fun updateUiWhenRideDone() {
        binding.callUberBtn.text = getString(R.string.call_ucar)
        binding.callUberBtn.beVisible()
        binding.icNotifyMinimal.root.beGone()
        binding.icNotifyExpand.root.beGone()
    }
    //endregion
}