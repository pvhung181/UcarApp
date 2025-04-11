package com.pvhung.ucar.ui.driver.driver_home

import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentDriverHomeBinding
import com.pvhung.ucar.ui.adapter.PagesAdapter
import com.pvhung.ucar.ui.base.BaseBindingFragment
import com.pvhung.ucar.utils.OnBackPressed

class DriverHomeFragment : BaseBindingFragment<FragmentDriverHomeBinding, DriverHomeViewModel>() {

    override fun getViewModel(): Class<DriverHomeViewModel> {
        return DriverHomeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_driver_home

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        OnBackPressed.onBackPressedFinishActivity(requireActivity(), this)
        initView()
    }

    private fun initView() {
        val adapter = PagesAdapter(this)
        binding.bottomNav.menu.forEach {
            val view = binding.bottomNav.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
        }

        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 4
        binding.viewPager2.isUserInputEnabled = false

        binding.bottomNav.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        for (i in 0 until binding.bottomNav.menu.size()) {
            binding.bottomNav.findViewById<BottomNavigationItemView>(
                binding.bottomNav.menu.getItem(
                    i
                ).itemId
            ).setOnLongClickListener {
                true
            }
        }

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.phoneFragment -> {
                    binding.viewPager2.setCurrentItem(0, false)
                }

                R.id.mActivityFragment -> {
                    binding.viewPager2.setCurrentItem(1, false)
                }

                R.id.mNotificationFragment -> {
                    binding.viewPager2.setCurrentItem(2, false)
                }

                R.id.mAccountFragment -> {
                    binding.viewPager2.setCurrentItem(3, false)
                }
            }
            true
        }
    }

    override fun observerData() {

    }

    override fun needInsetTop(): Boolean {
        return true
    }
}