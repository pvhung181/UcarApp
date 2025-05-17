package com.pvhung.ucar.ui.driver.driver_notification

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.FragmentDriverNotificationBinding
import com.pvhung.ucar.ui.adapter.RecentHistoryAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.CostUtils
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed
import com.pvhung.ucar.utils.getNumberValue

class DriverNotificationFragment :
    BaseBindingFragment<FragmentDriverNotificationBinding, DriverNotificationViewModel>() {

    override fun getViewModel(): Class<DriverNotificationViewModel> {
        return DriverNotificationViewModel::class.java
    }

    private var adapter: RecentHistoryAdapter? = null

    override val layoutId: Int
        get() = R.layout.fragment_driver_notification

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        init()
        initView()
        onClick()
    }

    private fun init() {
        adapter = RecentHistoryAdapter(requireContext())
        binding.rcvRecent.adapter = adapter
    }

    private fun initView() {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabaseUtils.getDriverHistoryDatabase(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lst = mutableListOf<HistoryItem>()
                        for (item in snapshot.children) {
                            val history = item.getValue(HistoryItem::class.java)
                            if (history != null) lst.add(history)
                        }
                        lst.sortByDescending { it.time }
                        adapter?.submit(lst.take(3))
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabaseUtils.getCurrentDriverDatabase().child("Earnings")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val earnings = snapshot.getNumberValue()
                        binding.tvEarnings.text = CostUtils.formatCurrency(earnings.toFloat())
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    private fun onClick() {

    }

    override fun observerData() {

    }
}