package com.pvhung.ucar.ui.customer.customer_map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
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
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.PermissionHelper
import com.pvhung.ucar.utils.Utils
import com.pvhung.ucar.utils.ViewUtils
import com.pvhung.ucar.utils.beGone
import com.pvhung.ucar.utils.beVisible
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


class CustomerMapFragment : BaseBindingFragment<FragmentCustomerMapBinding, CustomerMapViewModel>(),
    OnMapReadyCallback {

    private val enableGpsDialog by lazy { EnableGpsDialog(requireContext()) }
    private val searchingDriverDialog by lazy { SearchingDriverDialog(requireContext()) }
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


    lateinit var autoCompleteFragment: AutocompleteSupportFragment
    lateinit var autoCompletePickupFragment: AutocompleteSupportFragment

    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""

    private val selectPaymentDialog by lazy {
        SelectPaymentMethodDialog(
            requireContext(),
            object : SelectPaymentMethodDialog.SelectPaymentListener {
                override fun onPayInCash() {
                    requestUcar()
                }

                override fun onPayWithPaypal() {
                    startOrder()
                }
            }
        )
    }

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
        setupAutoComplete()
        initBottomsheet()
        fetchAccessToken()
    }

    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")

                    Toast.makeText(requireActivity(), "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: ANError) {
                    Toast.makeText(requireActivity(), "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "${requestModel.cost}")
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "Ucar Pvhung")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", Constant.returnUrl)
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    showToast("Order Error")
                }
            })
    }

    private fun handlerOrderID(orderID: String) {
        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
        val payPalWebCheckoutClient =
            PayPalWebCheckoutClient(requireActivity(), config, Constant.returnUrl)
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                showToast("Order success")
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                showToast("Order Error")

            }

            override fun onPayPalWebCanceled() {
                // showToast("Order cancel")

            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)
    }

    private fun initBottomsheet() {
        bookingBottomSheet = BottomSheetBookingVehicle(
            requireActivity(),
            object : BottomSheetBookingVehicle.OnRequestListener {
                override fun onBook(service: String) {
                    serviceBooking = service
                    requestModel.cost = bookingBottomSheet?.getCost() ?: 0f
                    selectPaymentDialog.show()
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
                if (requestModel.destination != "" && requestModel.pickupLocation != "") binding.callUberBtn.beVisible()
                else binding.callUberBtn.beGone()
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
                if (requestModel.destination != "" && requestModel.pickupLocation != "") binding.callUberBtn.beVisible()
                else binding.callUberBtn.beGone()
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
        mainViewModel.captureOrder.observe(viewLifecycleOwner) {
            if (it != null && it == true) {
                captureOrder(orderid)
                mainViewModel.captureOrder.value = null
            }
        }
    }

    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Toast.makeText(requireActivity(), "Payment Successful", Toast.LENGTH_SHORT)
                        .show()
                    requestUcar()

                }

                override fun onError(error: ANError) {
                    Toast.makeText(requireActivity(), "Payment Fail", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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
                UserBookingState.IDLE -> {
                    if (requestModel.destination.isNotBlank())
                        bookingBottomSheet?.show()
                    else showToast("Vui lòng chọn điểm đến !")
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
                                                            showToast("Accepted")
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
                                                            showToast("Driver cancel")
                                                        }

                                                    }
                                                } else {
                                                    if (bookState == UserBookingState.RIDING)
                                                        updateWhenRideDone()
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
            it.tvVehicle.text = user.getService()
            it.tvDistance.text = String.format("%.2f km", requestModel.distance)
            it.tvMoney.text = String.format("$%.2f", requestModel.cost)
        }

        binding.icNotifyExpand.ivMessage.setOnClickListener {
            IntentManager.goToMessage(requireActivity(), user.phoneNumber)
        }

        binding.icNotifyExpand.ivCall.setOnClickListener {
            IntentManager.goToCall(requireActivity(), user.phoneNumber)
        }
    }


    fun updateWhenDriverArrived() {
        binding.icNotifyExpand.btnRide.text = getString(R.string.arrived)
        bookState = UserBookingState.RIDING
    }

    fun updateWhenFoundDriver() {
        if (!isChangeUiWhenDriverFound) {
            binding.callUberBtn.beGone()
            searchingDriverDialog.dismiss()
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
        if (::driveHasEndedRef.isInitialized && ::driveHasEndedRefListener.isInitialized)
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener)
        radius = 1.0
        foundDriverId?.let {
            isFoundDriver = false
            foundDriverId = null
        }
        pickupMarker?.remove()
        bookState = UserBookingState.IDLE
        Utils.removeRequest()
        requestModel.reset()
        if (::autoCompleteFragment.isInitialized) autoCompleteFragment.setText("")
        if (::autoCompletePickupFragment.isInitialized) autoCompletePickupFragment.setText("")
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
                    mMap.addMarker(
                        MarkerOptions().position(pickupLocation!!).title("Pick up here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))
                    )
                getClosestDriver()
            }
        }
    }

    private fun cancelRequest() {
        bookState = UserBookingState.IDLE
        updateWhenRideDone()
    }


    private lateinit var driveHasEndedRef: DatabaseReference
    private lateinit var driveHasEndedRefListener: ValueEventListener
    private fun getHasRideEnded() {
        if (foundDriverId != null) {
            driveHasEndedRef = FirebaseDatabaseUtils.getSpecificRiderDatabase(foundDriverId!!)
                .child(Constant.REQUEST_STATE_REFERENCES)

            driveHasEndedRefListener =
                driveHasEndedRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                        } else {
                            updateWhenRideDone()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
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
        binding.callUberBtn.beGone()
        binding.icNotifyMinimal.root.beGone()
        binding.icNotifyExpand.root.beGone()
    }
    //endregion
}