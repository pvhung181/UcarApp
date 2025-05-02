package com.pvhung.ucar.ui.driver.driver_activity

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.R
import com.pvhung.ucar.data.model.HistoryItem
import com.pvhung.ucar.databinding.FragmentDriverActivityBinding
import com.pvhung.ucar.ui.adapter.HistoryAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import com.pvhung.ucar.utils.OnBackPressed

class DriverActivityFragment :
    BaseBindingFragment<FragmentDriverActivityBinding, DriverActivityViewModel>() {

    private lateinit var historyAdapter: HistoryAdapter

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
        historyAdapter = HistoryAdapter(requireActivity())
        binding.rcvHistory.adapter = historyAdapter

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseDatabaseUtils.getDriverHistoryDatabase(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lst = mutableListOf<HistoryItem>()
                        for (item in snapshot.children) {
                            val history = item.getValue(HistoryItem::class.java)
                            if(history != null) lst.add(history)
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
}