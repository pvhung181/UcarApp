package com.pvhung.ucar.ui.driver.driver_activity

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.FragmentDriverActivityBinding
import com.pvhung.ucar.ui.adapter.HistoryAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.ui.dialog.RatingRideDialog
import com.pvhung.ucar.ui.dialog.ViewRatingDialog
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed

class DriverActivityFragment :
    BaseBindingFragment<FragmentDriverActivityBinding, DriverActivityViewModel>(),
    RatingRideDialog.RatingListener {


    private lateinit var historyAdapter: HistoryAdapter
    private val ratingDialog by lazy { RatingRideDialog(requireContext(), this) }
    private val viewRatingDialog by lazy { ViewRatingDialog(requireContext()) }

    override fun getViewModel(): Class<DriverActivityViewModel> {
        return DriverActivityViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_activity

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        init()
        initView()
        onClick()
    }

    private fun init() {
        historyAdapter = HistoryAdapter(
            requireActivity(),
            isDriver = true,
            object : HistoryAdapter.HistoryClickListener {
                override fun onReviewClick(historyItem: HistoryItem) {
                    ratingDialog.setHistoryItem(historyItem)
                    ratingDialog.show()
                }

                override fun onViewReviewClick(rating: Int, review: String) {
                    viewRatingDialog.setReview(rating, review)
                    viewRatingDialog.show()
                }
            })
        binding.rcvHistory.adapter = historyAdapter

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabaseUtils.getDriverHistoryDatabase(it)
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

    }

    private fun onClick() {

    }

    override fun onSubmit(historyItem: HistoryItem, rating: Int, review: String) {
        if (rating < 1) {
            showToast("Bạn phải đánh giá dịch vụ !")
        } else {
            val dr =
                FirebaseDatabaseUtils.getSpecificRiderDatabase(historyItem.driverId).child(Constant.HISTORY_REFERENCES).child(historyItem.id)
            val cr = FirebaseDatabaseUtils.getSpecificCustomerDatabase(historyItem.customerId)
                .child(Constant.HISTORY_REFERENCES).child(historyItem.id)
            val hr = FirebaseDatabaseUtils.getHistoryDatabase().child(historyItem.id)
            val map = mutableMapOf<String, Any>(
                "rating" to rating,
                "review" to review
            )
            dr.updateChildren(map)
            cr.updateChildren(map)
            hr.updateChildren(map)
        }
    }

    override fun onCancel() {

    }
}