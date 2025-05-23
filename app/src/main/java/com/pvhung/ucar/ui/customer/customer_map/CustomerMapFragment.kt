package com.pvhung.ucar.ui.customer.customer_map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.directions.route.AbstractRouting
import com.directions.route.Route
import com.directions.route.RouteException
import com.directions.route.Routing
import com.directions.route.RoutingListener
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.android.gms.common.api.Status
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import com.pvhung.ucar.App
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.common.Constant.clientID
import com.pvhung.ucar.common.Constant.secretID
import com.pvhung.ucar.common.enums.RequestState
import com.pvhung.ucar.common.enums.RideState
import com.pvhung.ucar.common.enums.UserBookingState
import com.pvhung.ucar.data.model.RequestModel
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.databinding.FragmentCustomerMapBinding
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.dialog.BottomSheetBookingVehicle
import com.pvhung.ucar.ui.dialog.EnableGpsDialog
import com.pvhung.ucar.ui.dialog.SearchingDriverDialog
import com.pvhung.ucar.ui.dialog.SelectPaymentMethodDialog
import com.pvhung.ucar.utils.CostUtils
import com.pvhung.ucar.utils.DeviceHelper
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.IntentManager
import com.pvhung.ucar.utils.MethodUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper
import com.pvhung.ucar.utils.Utils
import com.pvhung.ucar.utils.ViewUtils
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beVisible
import com.pvhung.ucar.utils.getStringValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


class CustomerMapFragment : BaseBindingFragment<FragmentCustomerMapBinding, CustomerMapViewModel>(),
    OnMapReadyCallback, RoutingListener {

    private val enableGpsDialog by lazy { EnableGpsDialog(requireContext()) }
    private val searchingDriverDialog by lazy {
        SearchingDriverDialog(requireContext(), object : SearchingDriverDialog.ItemClickListener {
            override fun onCancelClick() {
                cancelRequest()
            }
        })
    }
    private val selectPaymentMethod by lazy {
        SelectPaymentMethodDialog(
            requireContext(),
            object : SelectPaymentMethodDialog.SelectPaymentListener {
                override fun onPayInCash() {
                    requestModel.isAlreadyPaid = true
                    requestModel.paymentMethod = "Cash"
                    requestUcar()
                }

                override fun onPayWithPaypal() {
                    requestModel.isAlreadyPaid = false
                    requestModel.paymentMethod = "Paypal"
                    requestUcar()
                }
            }

        )
    }

    private var isChangeUiWhenDriverFound = false
    private var bookState = UserBookingState.IDLE
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
    private var serviceBooking = Constant.MOTOBIKE_SERVICE
    private var bookingBottomSheet: BottomSheetBookingVehicle? = null

    //Use for looking driver
    private var geoQuery: GeoQuery? = null
    private var driverLocationRef: DatabaseReference? = null
    private var driverLocationRefListener: ValueEventListener? = null

    private val requestModel = RequestModel()
    private var pickupMarker: Marker? = null
    private var startMarker: Marker? = null
    private var destinationMarker: Marker? = null


    lateinit var autoCompleteFragment: AutocompleteSupportFragment
    lateinit var autoCompletePickupFragment: AutocompleteSupportFragment



    private var customerStateRef: DatabaseReference? = null
    private var customerStateListener: ValueEventListener? = null

    private var polylines = mutableListOf<Polyline>()
    private val COLORS: IntArray = intArrayOf(
        R.color.primary_dark,
        R.color.primary,
        R.color.primary_light,
        R.color.accent
    )

    @SuppressLint("MissingPermission")
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
            if(::mMap.isInitialized) {
                mMap.uiSettings.isZoomControlsEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mMap.isMyLocationEnabled = true
            }

        } else {

        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult != null) {
                locationResult.lastLocation?.let {
                    if (mLastLocation == null) {
                        locationResult.lastLocation?.let {
                            viewModel.getAddressFromLocation(
                                requireContext(),
                                it.latitude,
                                it.longitude,
                            ) { place ->
                                requestModel.pickupLat = it.latitude
                                requestModel.pickupLng = it.longitude
                                requestModel.pickupLocation = place ?: ""
                                autoCompletePickupFragment.setText(place)
                            }
                        }
                    }

                    val newPos = LatLng(it.latitude, it.longitude)
                    if (mLastLocation == null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 19f))
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(19f))
                    }
                    mLastLocation = it
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
        observerCustomerState()
        initMaps()
        init()
        initView()
        onClick()
        setupAutoComplete()
        initBottomsheet()
    }

    private fun observerCustomerState() {
        if (customerStateRef == null && customerStateListener == null) {
            customerStateRef = FirebaseDatabaseUtils.getCurrentCustomerDatabase().child("State")
            customerStateListener =
                customerStateRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val state = snapshot.getStringValue()
                        when (state) {

                            RideState.IDLE.toString() -> {

                            }

                            RideState.MOVING.toString() -> {
                                showToast("MOVING")

                            }

                            RideState.PICKED.toString() -> {
                                showToast("PICKED")
                                pickupMarker?.remove()
                                startMarker?.remove()
                                mDriverMarker?.remove()
                                animeToCurrent(18f)
//                                getRouteToMarker(
//                                    LatLng(
//                                        requestModel.destinationLat,
//                                        requestModel.destinationLng
//                                    )
//                                )
//                                updateWhenDriverArrived()
                            }

                            RideState.RIDING.toString() -> {
                                showToast("RIDING")

                            }

                            RideState.DONE.toString() -> {
                                showToast("DONE")
                                erasePolyLines()
                                startMarker?.remove()
                                destinationMarker?.remove()
                                updateWhenRideDone()
                                FirebaseDatabaseUtils.getCurrentCustomerDatabase().child("State")
                                    .setValue(RideState.IDLE)

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    }


    private fun initBottomsheet() {
        bookingBottomSheet = BottomSheetBookingVehicle(
            requireActivity(),
            object : BottomSheetBookingVehicle.OnRequestListener {
                override fun onBook(service: String) {
                    serviceBooking = service
                    requestModel.cost = bookingBottomSheet?.getCost() ?: 0f
                    selectPaymentMethod.show()
                }

                override fun onDismiss() {}
            }
        )
    }

    private fun setupAutoComplete() {
        if (!Places.isInitialized()) {
            Places.initialize(App.instance, Constant.MAPS_API)
        }


        autoCompleteFragment =
            ((childFragmentManager.findFragmentById(R.id.autocompleteFragment)) as AutocompleteSupportFragment).setPlaceFields(
                listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            )
        autoCompletePickupFragment =
            ((childFragmentManager.findFragmentById(R.id.autocompletePickupFragment)) as AutocompleteSupportFragment).setPlaceFields(
                listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            )
        autoCompleteFragment.setCountries("vn")
        autoCompletePickupFragment.setCountries("vn")
        try {
            val autocompleteTextView = (autoCompleteFragment.view?.findViewById<View>(
                com.google.android.libraries.places.R.id.places_autocomplete_search_input
            ) as? EditText)
            autocompleteTextView?.apply {
                setHintTextColor(Color.GRAY)
                setTextColor(Color.BLACK)
                setHint("Destination address")
                background = null
                textSize = 16f
                setPadding(
                    ViewUtils.dpToPx(requireContext(), 8f),
                    ViewUtils.dpToPx(requireContext(), 12f),
                    ViewUtils.dpToPx(requireContext(), 8f),
                    ViewUtils.dpToPx(requireContext(), 12f)
                )
            }

            val searchIcon = autoCompleteFragment.view?.findViewById<View>(
                com.google.android.libraries.places.R.id.places_autocomplete_search_button
            )
            searchIcon?.visibility = View.GONE
            /////////////////////////////////////////
            val tv = (autoCompletePickupFragment.view?.findViewById<View>(
                com.google.android.libraries.places.R.id.places_autocomplete_search_input
            ) as? EditText)
            tv?.apply {
                setHintTextColor(Color.GRAY)
                setTextColor(Color.BLACK)
                setHint("Pickup address")
                background = null
                textSize = 16f
                setPadding(
                    ViewUtils.dpToPx(requireContext(), 8f),
                    ViewUtils.dpToPx(requireContext(), 12f),
                    ViewUtils.dpToPx(requireContext(), 8f),
                    ViewUtils.dpToPx(requireContext(), 12f)
                )
            }

            val si = autoCompletePickupFragment.view?.findViewById<View>(
                com.google.android.libraries.places.R.id.places_autocomplete_search_button
            )
            si?.visibility = View.GONE
        } catch (_: Exception) {
        }


        autoCompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(e: Status) {
                e.statusMessage?.let { showToast(it) }
            }

            override fun onPlaceSelected(place: Place) {
                requestModel.destination = place.name?.toString() ?: ""
                place.latLng?.let {
                    requestModel.destinationLat = it.latitude
                    requestModel.destinationLng = it.longitude
                }
                if (requestModel.destination != "" && requestModel.pickupLocation != "") {
                    binding.callUberBtn.beVisible()
                    showLine()
                } else binding.callUberBtn.beGone()
                if (place.latLng != null) {

                    val distance = getDistance(
                        Location("").apply {
                            longitude = requestModel.pickupLng
                            latitude = requestModel.pickupLat
                        },
                        Location("").apply {
                            longitude = requestModel.destinationLng
                            latitude = requestModel.destinationLat
                        }
                    ).toDouble()
                    requestModel.distance = distance / 1000f
                    bookingBottomSheet?.setCost(
                        CostUtils.getCost(distance.toFloat()),
                        CostUtils.getCarCost(distance.toFloat())
                    )
                }

            }
        })



        autoCompletePickupFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(e: Status) {
                e.statusMessage?.let { showToast(it) }
            }

            override fun onPlaceSelected(place: Place) {
                requestModel.pickupLocation = place.name?.toString() ?: ""
                place.latLng?.let {
                    requestModel.pickupLat = it.latitude
                    requestModel.pickupLng = it.longitude
                }
                if (requestModel.destination != "" && requestModel.pickupLocation != "") {
                    binding.callUberBtn.beVisible()
                    showLine()
                } else binding.callUberBtn.beGone()
                if (place.latLng != null) {

                    val distance = getDistance(
                        Location("").apply {
                            longitude = requestModel.pickupLng
                            latitude = requestModel.pickupLat
                        },
                        Location("").apply {
                            longitude = requestModel.destinationLng
                            latitude = requestModel.destinationLat
                        }
                    ).toDouble()
                    requestModel.distance = distance / 1000f
                    bookingBottomSheet?.setCost(
                        CostUtils.getCost(distance.toFloat()),
                        CostUtils.getCarCost(distance.toFloat())
                    )
                }
            }
        })
    }

    fun showLine() {
        pickupMarker?.remove()
        startMarker?.remove()

        erasePolyLines()
        startMarker =
            mMap.addMarker(
                MarkerOptions().position(LatLng(requestModel.pickupLat, requestModel.pickupLng))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
            )
        destinationMarker =
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        requestModel.destinationLat,
                        requestModel.destinationLng
                    )
                )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
            )

        getRouteToMarker(
            LatLng(requestModel.pickupLat, requestModel.pickupLng),
            LatLng(requestModel.destinationLat, requestModel.destinationLng)
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    requestModel.pickupLat,
                    requestModel.pickupLng
                ), 12f
            )
        )
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
    }

    fun animeToCurrent(zoom: Float) {
        mLastLocation?.let {
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.latitude,
                        it.longitude
                    ), zoom
                )
            )
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom))
        }
    }

    private fun getDistance(start: Location, end: Location): Float {
        return start.distanceTo(end)
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

        binding.ivMyLocation.setOnClickListener {
            mLastLocation?.let {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        19f
                    )
                )
                mMap.animateCamera(CameraUpdateFactory.zoomTo(19f))
            }
        }

        binding.callUberBtn.setOnClickListener {
            when (bookState) {
                UserBookingState.IDLE -> {
                    if (requestModel.destination.isNotBlank())
                        bookingBottomSheet?.show()
                    else showToast("Vui lòng chọn điểm đến !")
                }

                else -> {
//                    cancelRequest()
                }
            }

        }
    }


    override fun onResume() {
        super.onResume()
        if (PermissionHelper.isGpsEnabled(requireContext()) && enableGpsDialog.isShowing) enableGpsDialog.dismiss()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(gm: GoogleMap) {
        mMap = gm

        mMap.setOnMapClickListener {

        }

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
                mMap.uiSettings.isZoomControlsEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mMap.isMyLocationEnabled = true
            } else {
                PermissionHelper.requestLocationPermission(locationPermissionLauncher)
            }
        }

    }

    private fun getClosestDriver() {
        searchingDriverDialog.show()
        bookState = UserBookingState.LOOKING
        val ref = FirebaseDatabaseUtils.getDriverAvailableDatabase()
        val geo = GeoFire(ref)
        pickupLocation?.let {
            geoQuery = geo.queryAtLocation(GeoLocation(it.latitude, it.longitude), radius)
            geoQuery?.removeAllListeners()
            geoQuery?.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
                override fun onDataEntered(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                    if (!isFoundDriver && dataSnapshot?.key != null && bookState == UserBookingState.LOOKING) {
                        val db = FirebaseDatabaseUtils.getSpecificRiderDatabase(dataSnapshot.key!!)

                        db.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (isFoundDriver) return
                                val user = FirebaseDatabaseUtils.getUserFromSnapshot(snapshot)
                                if (user != null && user.getService() == serviceBooking) {
                                    isFoundDriver = true
                                    foundDriverId = dataSnapshot.key

                                    val driverRef =
                                        FirebaseDatabaseUtils.getSpecificRiderDatabase(foundDriverId!!)
                                            .child(Constant.REQUEST_STATE_REFERENCES)
                                    val customerId = FirebaseAuth.getInstance().currentUser?.uid!!
                                    requestModel.customerId = customerId
                                    driverRef.setValue(requestModel)
                                    bookState = UserBookingState.PENDING
                                    if (!isWaitingResponse) {
                                        isWaitingResponse = true
                                        driverRef.addValueEventListener(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val request =
                                                        snapshot.getValue(RequestModel::class.java)
                                                    if (request != null) {
                                                        if (request.state == RequestState.ACCEPT) {
                                                            getDriverLocation()
                                                            Utils.removeRequest()
                                                            updateNotiDriverInfo(user, request)
                                                            isWaitingResponse = false
                                                            bookState = UserBookingState.FOUND
                                                        } else if (request.state == RequestState.CANCEL) {
                                                            driverRef.removeValue()
                                                            isFoundDriver = false
                                                            foundDriverId = ""
                                                            driverRef.removeEventListener(this)
                                                            bookState = UserBookingState.LOOKING
                                                        }

                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}

                                        })
                                    }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })

                    }
                }

                override fun onDataExited(dataSnapshot: DataSnapshot?) {

                }

                override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {

                }

                override fun onGeoQueryReady() {
                    if (!isFoundDriver && bookState == UserBookingState.LOOKING) {
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

                        mDriverMarker?.remove()
                        mDriverMarker = mMap.addMarker(
                            MarkerOptions().position(driverLocation).title("Your driver")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun updateNotiDriverInfo(user: User, requestModel: RequestModel) {
        binding.icNotifyMinimal.let {
            it.tvName.text = user.fullName
            it.tvRating.text = String.format("%.1f", user.rating)
            it.tvVehicle.text = user.getService()
        }

        binding.icNotifyExpand.let {
            it.tvName.text = user.fullName
            it.tvRating.text = String.format("%.1f", user.rating)
            it.tvVehicle.text = "${user.getService()} | ${user.numberPlate}"
            it.tvDistance.text = String.format("%.2f km", requestModel.distance)
            it.tvMoney.text = CostUtils.formatCurrency(requestModel.cost.toFloat())
            if (user.avatar != "") {
                try {
                    val bm = MethodUtils.base64ToBitmap(user.avatar)
                    binding.icNotifyExpand.ivAvatar.setImageBitmap(bm)
                    binding.icNotifyMinimal.ivAvatar.setImageBitmap(bm)
                } catch (_: Exception) {
                }
            } else {
                binding.icNotifyExpand.ivAvatar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.iv_male_avatar
                    )
                )

            }
        }

        binding.icNotifyExpand.ivMessage.setOnClickListener {
            IntentManager.goToMessage(requireActivity(), user.phoneNumber)
        }

        binding.icNotifyExpand.ivCall.setOnClickListener {
            IntentManager.goToCall(requireActivity(), user.phoneNumber)
        }
    }


    fun updateWhenDriverArrived() {
        bookState = UserBookingState.RIDING
    }

    fun updateWhenFoundDriver() {
        if (!isChangeUiWhenDriverFound) {
            binding.callUberBtn.beGone()
            searchingDriverDialog.dismiss()
            binding.icNotifyMinimal.root.beVisible()
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
//        if (::driveHasEndedRef.isInitialized && ::driveHasEndedRefListener.isInitialized)
//            driveHasEndedRef.removeEventListener(driveHasEndedRefListener)
        radius = 1.0
        foundDriverId?.let {
            isFoundDriver = false
            foundDriverId = null
        }
        pickupMarker?.remove()
        mDriverMarker?.remove()

        bookState = UserBookingState.IDLE
        Utils.removeRequest()
        requestModel.reset()
        if (::autoCompleteFragment.isInitialized) autoCompleteFragment.setText("")
        if (::autoCompletePickupFragment.isInitialized) autoCompletePickupFragment.setText("")
        customerStateRef?.setValue(RideState.IDLE)
        mLastLocation?.let {
            viewModel.getAddressFromLocation(
                requireContext(),
                it.latitude,
                it.longitude,
            ) { place ->
                requestModel.pickupLat = it.latitude
                requestModel.pickupLng = it.longitude
                requestModel.pickupLocation = place ?: ""
                autoCompletePickupFragment.setText(place)
            }
        }
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

    fun getRouteToMarker(st: LatLng, l: LatLng) {
        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .key(Constant.MAPS_API)
            .alternativeRoutes(false)
            .waypoints(st, l)
            .build()
        routing.execute()
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
//                pickupMarker =
//                    mMap.addMarker(
//                        MarkerOptions().position(pickupLocation!!).title("Pick up here")
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))
//                    )
                getClosestDriver()
            }
        }
    }

    private fun cancelRequest() {
        bookState = UserBookingState.IDLE
        startMarker?.remove()
        destinationMarker?.remove()
        erasePolyLines()
        updateWhenRideDone()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {

        } catch (_: Exception) {
        }
    }

    //region routing
    private fun erasePolyLines() {
        polylines.forEach {
            it.remove()
        }
        polylines.clear()

    }

    override fun onRoutingFailure(p0: RouteException?) {

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
    //endregion

    //region update ui
    private fun updateUiWhenRideDone() {
        binding.callUberBtn.text = getString(R.string.call_ucar)
        binding.callUberBtn.beGone()
        binding.icNotifyMinimal.root.beGone()
        binding.icNotifyExpand.root.beGone()
    }

    //endregion
}