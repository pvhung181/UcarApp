package com.pvhung.ucar.ui.driver.driver_map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.directions.route.AbstractRouting
import com.directions.route.Route
import com.directions.route.RouteException
import com.directions.route.Routing
import com.directions.route.RoutingListener
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.App
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.common.enums.DriverRideState
import com.pvhung.ucar.common.enums.RequestState
import com.pvhung.ucar.data.model.RequestModel
import com.pvhung.ucar.databinding.FragmentDriverMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beInvisible
import com.pvhung.ucar.utils.beVisible


class MapFragment : BaseBindingFragment<FragmentDriverMapBinding, MapViewModel>(),
    OnMapReadyCallback, RoutingListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val requestModel = RequestModel()
    private var rideState = DriverRideState.IDLE
    private var pickupLocation: Marker? = null
    private var polylines = mutableListOf<Polyline>()
    private val COLORS: IntArray = intArrayOf(
        R.color.primary_dark,
        R.color.primary,
        R.color.primary_light,
        R.color.accent
    )

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
        override fun onLocationResult(locationResult: LocationResult) {
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

                        when (requestModel.customerId) {
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
        get() = R.layout.fragment_driver_map

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        polylines = mutableListOf()
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
            .child(Constant.REQUEST_STATE_REFERENCES)

        assignedCustomerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val request = snapshot.getValue(RequestModel::class.java)

                    if (!binding.icUserRequest.root.isVisible && request?.state == RequestState.IDLE) binding.icUserRequest.root.beVisible()
                    if (request != null) {
                        if (isAdded)
                            binding.icUserRequest.tvDestination.text = request.destination
                    }
                    if (request != null && request.state == RequestState.ACCEPT) {
                        rideState = DriverRideState.MOVING
                        requestModel.customerId = request.customerId
                        requestModel.destination = request.destination
                        requestModel.destinationLat = request.destinationLat
                        requestModel.destinationLng = request.destinationLng
                        binding.icUserInfo.apply {
                            root.beVisible()
                            tvName.text = requestModel.customerId
                            tvDestination.text = requestModel.destination
                        }
                        getAssignedCustomerPickupLocation()
                    } else if (request != null && request.state == RequestState.CANCEL) {
                        rideState = DriverRideState.IDLE
                        assignedCustomerRef.removeValue()
                    }
                } else {
                    endRide()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var assignedCustomerPickupRef: DatabaseReference? = null
    private var assignedCustomerPickupRefListener: ValueEventListener? = null

    private fun getAssignedCustomerPickupLocation() {
        assignedCustomerPickupRef =
            FirebaseDatabaseUtils.getRequestsDatabase().child(requestModel.customerId).child("l")

        assignedCustomerPickupRefListener =
            assignedCustomerPickupRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && requestModel.customerId != "") {
                        val map = snapshot.value as List<*>
                        var locationLat = 0.0
                        var locationLng = 0.0
                        if (map[0] != null) {
                            locationLat = (map[0].toString()).toDouble()
                        }
                        if (map[1] != null) {
                            locationLng = (map[1].toString()).toDouble()
                        }
                        var pickupLatLng = LatLng(locationLat, locationLng)
                        pickupLocation = mMap.addMarker(
                            MarkerOptions().position(pickupLatLng).title("Pickup location").icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin)
                            )
                        )
                        getRouteToMarker(pickupLatLng)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun getRouteToMarker(l: LatLng) {
        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .key(Constant.MAPS_API)
            .alternativeRoutes(false)
            .waypoints(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude), l)
            .build()
        routing.execute()
    }

    private fun changeUiBasedOnRideState() {
        when (rideState) {
            DriverRideState.IDLE -> {

            }

            DriverRideState.MOVING -> {

            }

            DriverRideState.RIDING -> {

            }

            DriverRideState.DONE -> {

            }
        }
    }

    private fun initMaps() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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
        binding.icUserRequest.ivAccept.setOnClickListener {
            val db = getDb().child("state")
            db.setValue(RequestState.ACCEPT)
            binding.icUserRequest.root.beGone()
        }

        binding.icUserRequest.tvCancel.setOnClickListener {
            val db = getDb().child("state")
            db.setValue(RequestState.CANCEL)
            binding.icUserRequest.root.beGone()
        }

        binding.icUserInfo.rideStatus.setOnClickListener {
            when (rideState) {
                DriverRideState.IDLE -> {}

                DriverRideState.MOVING -> {
                    rideState = DriverRideState.RIDING
                    pickupLocation?.remove()
                    binding.icUserInfo.rideStatus.setText(getString(R.string.confirm_done))
                    erasePolyLines()
                    if (requestModel.destinationLat != 0.0 && requestModel.destinationLng != 0.0) {
                        getRouteToMarker(LatLng(requestModel.destinationLat, requestModel.destinationLng))
                    }
                }

                DriverRideState.RIDING -> {
                    saveRide()
                    endRide()
                }

                DriverRideState.DONE -> {}
            }
        }
    }

    private fun saveRide() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val dr = FirebaseDatabaseUtils.getSpecificRiderDatabase(uid)
        val cr = FirebaseDatabaseUtils.getSpecificCustomerDatabase(requestModel.customerId)
        val hr = FirebaseDatabaseUtils.getHistoryDatabase()
        val requestId = hr.push().key
        val map = mutableMapOf<String, Any>(
            "driver" to uid,
            "customer" to requestModel.customerId,
            "rating" to -1
        )

        hr.child(requestId!!).updateChildren(map)

    }

    private fun endRide() {
        FirebaseDatabaseUtils.getSpecificRiderDatabase(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(Constant.REQUEST_STATE_REFERENCES).removeValue()
        erasePolyLines()
        requestModel.reset()
        pickupLocation?.remove()
        assignedCustomerPickupRefListener?.let {
            assignedCustomerPickupRef?.removeEventListener(it)
        }
        rideState = DriverRideState.IDLE

        binding.icUserInfo.root.beInvisible()
    }


    private fun getDb(): DatabaseReference {
        val driverId = FirebaseAuth.getInstance().currentUser?.uid!!

        return FirebaseDatabaseUtils.getSpecificRiderDatabase(driverId)
            .child(Constant.REQUEST_STATE_REFERENCES)
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
                    mLocationRequest, mLocationCallback, Looper.myLooper()
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

    override fun onRoutingFailure(e: RouteException?) {
        if (e != null) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show();
            Log.e("pvhung111", "onRoutingFailure: ${e.message}")
        } else {
            Toast.makeText(requireContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT)
                .show();
        }
    }

    override fun onRoutingStart() {

    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, shortestIndex: Int) {
        route?.let {
            if (polylines?.isNotEmpty() == true) {
                for (poly in polylines!!) {
                    poly.remove()
                }
            }

            polylines = ArrayList<Polyline>()

            //add route(s) to the map.
            for (i in 0 until route.size) {
                //In case of more than 5 alternative routes

                val colorIndex: Int = i % COLORS.size

                val polyOptions = PolylineOptions()
                polyOptions.color(ContextCompat.getColor(requireContext(), COLORS[colorIndex]))
                polyOptions.width((10 + i * 3).toFloat())
                polyOptions.addAll(route[i].points)
                val polyline: Polyline = mMap.addPolyline(polyOptions)
                polylines.add(polyline)

                Toast.makeText(
                    App.instance,
                    ("Route " + (i + 1) + ": distance - " + route[i]
                        .distanceValue).toString() + ": duration - " + route[i]
                        .durationValue,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onRoutingCancelled() {

    }

    private fun erasePolyLines() {
        polylines.forEach {
            it.remove()
        }
        polylines.clear()

    }

}