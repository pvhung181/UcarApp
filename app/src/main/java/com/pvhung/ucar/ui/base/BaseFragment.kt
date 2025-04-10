package com.pvhung.ucar.ui.base

import androidx.fragment.app.Fragment
import com.pvhung.ucar.data.local.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {
    @Inject
    lateinit var mPrefHelper: SharedPreferenceHelper
}