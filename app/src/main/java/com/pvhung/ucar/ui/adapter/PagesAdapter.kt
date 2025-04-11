package com.pvhung.ucar.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pvhung.ucar.ui.driver.driver_account.DriverAccountFragment
import com.pvhung.ucar.ui.driver.driver_activity.DriverActivityFragment
import com.pvhung.ucar.ui.driver.driver_map.MapFragment
import com.pvhung.ucar.ui.driver.driver_notification.DriverNotificationFragment

class PagesAdapter(fr: Fragment) : FragmentStateAdapter(fr) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment()
            1 -> DriverActivityFragment()
            2 -> DriverNotificationFragment()
            else -> DriverAccountFragment()
        }
    }
}