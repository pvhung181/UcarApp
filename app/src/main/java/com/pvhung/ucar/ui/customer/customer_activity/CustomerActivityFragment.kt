package com.pvhung.ucar.ui.customer.customer_activity

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.common.Constant.clientID
import com.pvhung.ucar.common.Constant.secretID
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.FragmentCustomerActivityBinding
import com.pvhung.ucar.ui.adapter.HistoryAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.dialog.RatingRideDialog
import com.pvhung.ucar.ui.dialog.ViewRatingDialog
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.getNumberValue
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class CustomerActivityFragment :
    BaseBindingFragment<FragmentCustomerActivityBinding, CustomerActivityViewModel>(),
    RatingRideDialog.RatingListener {


    private lateinit var historyAdapter: HistoryAdapter
    private val ratingDialog by lazy { RatingRideDialog(requireContext(), this) }
    private val viewRatingDialog by lazy { ViewRatingDialog(requireContext()) }
    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""
    override fun getViewModel(): Class<CustomerActivityViewModel> {
        return CustomerActivityViewModel::class.java
    }

    private var historyItem: HistoryItem? = null

    override val layoutId: Int
        get() = R.layout.fragment_customer_activity

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        fetchAccessToken()
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        init()
        initView()
        onClick()
    }

    private fun init() {
        historyAdapter = HistoryAdapter(
            requireActivity(),
            isDriver = false,
            object : HistoryAdapter.HistoryClickListener {
                override fun onReviewClick(historyItem: HistoryItem) {
                    ratingDialog.setHistoryItem(historyItem)
                    ratingDialog.show()
                }

                override fun onViewReviewClick(rating: Int, review: String) {
                    viewRatingDialog.setReview(rating, review)
                    viewRatingDialog.show()
                }

                override fun onPaypalClick(historyItem: HistoryItem) {
                    if (accessToken.isEmpty()) {
                        showToast(getString(R.string.please_check_your_internet_connection))
                    } else {
                        this@CustomerActivityFragment.historyItem = historyItem
                        startOrder(historyItem.money.toFloat())
                    }
                }
            })
        binding.rcvHistory.adapter = historyAdapter

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabaseUtils.getCustomerHistoryDatabase(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lst = mutableListOf<HistoryItem>()
                        for (item in snapshot.children) {
                            val history = item.getValue(HistoryItem::class.java)
                            if (history != null) lst.add(history)
                        }
                        historyAdapter.submit(lst)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


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

    private fun onClick() {

    }

    override fun onSubmit(historyItem: HistoryItem, rating: Int, review: String) {
        if (rating < 1) {
            showToast("Bạn phải đánh giá dịch vụ !")
        } else {
//            ratingDialog.loading(true)
            val dr =
                FirebaseDatabaseUtils.getSpecificRiderDatabase(historyItem.driverId)
                    .child(Constant.HISTORY_REFERENCES).child(historyItem.id)
            val cr = FirebaseDatabaseUtils.getSpecificCustomerDatabase(historyItem.customerId)
                .child(Constant.HISTORY_REFERENCES).child(historyItem.id)
            val hr = FirebaseDatabaseUtils.getHistoryDatabase().child(historyItem.id)
            val map = mutableMapOf<String, Any>(
                "rating" to rating,
                "review" to review
            )
            dr.updateChildren(map)
                .addOnSuccessListener {
                    FirebaseDatabaseUtils.getDriverHistoryDatabase(historyItem.driverId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val lst = mutableListOf<HistoryItem>()
                                for (item in snapshot.children) {
                                    val history = item.getValue(HistoryItem::class.java)
                                    if (history != null) lst.add(history)
                                }
                                var totalRating = 0.0
                                for (item in lst) {
                                    totalRating += if (item.rating == 0.0) 5.0 else item.rating
                                }
                                totalRating /= lst.size
                                FirebaseDatabaseUtils.getSpecificRiderDatabase(historyItem.driverId)
                                    .child("rating").setValue(totalRating)
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
                .addOnCompleteListener { ratingDialog.dismiss() }
            cr.updateChildren(map).addOnCompleteListener { ratingDialog.dismiss() }
            hr.updateChildren(map).addOnCompleteListener { ratingDialog.dismiss() }
        }
    }

    override fun onCancel() {

    }

    //region paypal
    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Toast.makeText(requireActivity(),
                        getString(R.string.payment_successful), Toast.LENGTH_SHORT)
                        .show()
                    updatePaymentSuccessfully()
                }

                override fun onError(error: ANError) {
                    Toast.makeText(requireActivity(), "Payment Fail", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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

//                    Toast.makeText(requireActivity(), "Access Token Fetched!", Toast.LENGTH_SHORT)
//                        .show()
                }

                override fun onError(error: ANError) {
                    Toast.makeText(requireActivity(), "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startOrder(cost: Float) {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put(
                            "value", "${
                                BigDecimal((cost / 23000f).toDouble())
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toFloat()
                            }"
                        )
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

            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)
    }

    fun updatePaymentSuccessfully() {
        try {
            historyItem?.let { ht ->
                val dr =
                    FirebaseDatabaseUtils.getSpecificRiderDatabase(ht.driverId)
                        .child(Constant.HISTORY_REFERENCES).child(ht.id)
                        .child("paid")
                val cr =
                    FirebaseDatabaseUtils.getSpecificCustomerDatabase(ht.customerId)
                        .child(Constant.HISTORY_REFERENCES).child(ht.id)
                        .child("paid")
                val hr = FirebaseDatabaseUtils.getHistoryDatabase().child(ht.id)
                    .child(ht.id).child("paid")

                hr.setValue(true)
                dr.setValue(true)
                cr.setValue(true)
            }

        } catch (_: Exception) {

        }
    }

    //endregion
}