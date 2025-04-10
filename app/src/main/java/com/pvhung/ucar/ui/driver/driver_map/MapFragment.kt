//package com.pvhung.ucar.ui.driver.driver_map
//
//import android.annotation.SuppressLint
//import android.os.Bundle
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import com.firebase.geofire.GeoFire
//import com.firebase.geofire.GeoLocation
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.pvhung.ucar.R
//import com.pvhung.ucar.common.Constant
//import com.pvhung.ucar.databinding.FragmentMapBinding
//import com.pvhung.ucar.ui.base.BaseBindingFragment
//import com.pvhung.ucar.ui.dialog.EnableGpsDialog
//import com.pvhung.ucar.utils.OnBackPressed
//import com.pvhung.ucar.utils.PermissionHelper
//import com.pvhung.ucar.utils.beGone
//import com.pvhung.ucar.utils.beVisible
//
////todo fix uid null
//
//class MapFragment : BaseBindingFragment<FragmentMapBinding, MapViewModel>(), OnMapReadyCallback {
//
//    private lateinit var locationRequest: LocationRequest
//    private lateinit var locationCallback: LocationCallback
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private lateinit var mMap: GoogleMap
//    private lateinit var mapFragment: SupportMapFragment
//    private val enableGpsDialog: EnableGpsDialog by lazy { EnableGpsDialog(requireContext()) }
//    private var userPosition: Marker? = null
//
//    //online system
//    private lateinit var onlineRef: DatabaseReference
//    private lateinit var currentUserRef: DatabaseReference
//    private lateinit var driverLocationRef: DatabaseReference
//    private lateinit var geoFire: GeoFire
//
//    private val onlineValueEventListener = object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            if (snapshot.exists()) currentUserRef.onDisconnect().removeValue()
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            //Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
//        }
//
//    }
//
//    override fun getViewModel(): Class<MapViewModel> {
//        return MapViewModel::class.java
//    }
//
//    override val layoutId: Int
//        get() = R.layout.fragment_map
//
//    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        Log.e("pvhung1", "onCreatedView: ${mPrefHelper.getString(Constant.KEY_USER_ID)}", )
//        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
//        loading(true)
//        initMaps()
//        init()
//        initView()
//        onClick()
//    }
//
//    private fun initMaps() {
//        mapFragment = childFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun init() {
//        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")
//        driverLocationRef =
//            FirebaseDatabase.getInstance().getReference(Constant.DRIVERS_LOCATION_REFERENCES)
//        currentUserRef =
//            FirebaseDatabase.getInstance().getReference(Constant.DRIVERS_LOCATION_REFERENCES).child(
//                mPrefHelper.getString(Constant.KEY_USER_ID) ?: ""
//            )
//
//        geoFire = GeoFire(driverLocationRef)
//        registerOnlineSystem()
//
//        locationRequest = LocationRequest().apply {
//            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            fastestInterval = 3000
//            interval = 5000
//            smallestDisplacement = 10f
//        }
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                super.onLocationResult(locationResult)
//                locationResult.lastLocation?.let {
//                    val newPos = LatLng(it.latitude, it.longitude)
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
//                    geoFire.setLocation(
//                        mPrefHelper.getString(Constant.KEY_USER_ID) ?: "",
//                        GeoLocation(it.latitude, it.longitude)
//                    ) { k, e ->
//                        if (e != null)
////                            Snackbar.make(
////                                mapFragment.requireView(),
////                                e.message,
////                                Snackbar.LENGTH_LONG
////                            ).show()
//                        else {
////                            Snackbar.make(
////                                mapFragment.requireView(),
////                                "You are online",
////                                Snackbar.LENGTH_LONG
////                            ).show()
//
//                        }
//
//                    }
//                }
//            }
//        }
//
//
//        fusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(requireContext())
//        if (PermissionHelper.hasLocationPermission(requireContext())) {
//            fusedLocationProviderClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.myLooper()
//            )
//        } else {
//            PermissionHelper.requestLocationPermission(requireActivity(), onGranted = {
//
//            }, onFail = {})
//        }
//
//    }
//
//    private fun initView() {
//
//    }
//
//    override fun observerData() {
//
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun onClick() {
//        binding.currentLocationButton.setOnClickListener {
//            if (PermissionHelper.hasLocationPermission(requireContext())) {
//                if (PermissionHelper.isGpsEnabled(requireContext())) {
//                    loading(true)
//                    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                        loading(false)
//                        val userLatLng = LatLng(location.latitude, location.longitude)
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))
//                        userPosition?.remove()
//                        userPosition =
//                            mMap.addMarker(MarkerOptions().position(userLatLng).title("Test"))
//
//                    }
//                        .addOnCompleteListener { loading(false) }
//                } else {
//                    enableGpsDialog.show()
//                }
//            } else {
//                PermissionHelper.requestLocationPermission(
//                    requireActivity()
//                )
//            }
//        }
//    }
//
//    override fun onResume() {
//        if (PermissionHelper.isGpsEnabled(requireContext())) {
//            enableGpsDialog.dismiss()
//        }
//        super.onResume()
//        registerOnlineSystem()
//    }
//
//    private fun registerOnlineSystem() {
//        onlineRef.addValueEventListener(onlineValueEventListener)
//
//    }
//
//
//    private fun loading(isLoading: Boolean) {
//        if (isLoading) {
//            binding.viewBlock.beVisible()
//            binding.progress.beVisible()
//        } else {
//            binding.viewBlock.beGone()
//            binding.progress.beGone()
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onMapReady(gm: GoogleMap) {
//        loading(false)
//        mMap = gm
//        mMap.uiSettings.isZoomControlsEnabled = true
////        try {
////            gm.setMapStyle(
////                MapStyleOptions.loadRawResourceStyle(
////                    requireContext(),
////                    R.raw.uber_maps_style
////                )
////            )
////        } catch (_: Exception) {
////        }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
//            mPrefHelper.getString(Constant.KEY_USER_ID)?.let {
//                geoFire.removeLocation(it)
//            }
//            onlineRef.removeEventListener(onlineValueEventListener)
//
//        } catch (_: Exception) {
//        }
//    }
//
//
//}