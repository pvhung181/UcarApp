package com.pvhung.ucar.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pvhung.ucar.data.local.SharedPreferenceHelper
import com.pvhung.ucar.utils.LocaleUtils
import com.pvhung.ucar.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var mPrefHelper: SharedPreferenceHelper
    var statusBarHeight = 0
    var navigationHeight = 0
    protected var isSetupPaddingBottom = false
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleUtils.applyLocale(this)
        super.onCreate(savedInstanceState)
        statusBarHeight = ViewUtils.getStatusBarHeight()
        navigationHeight = ViewUtils.getNaviBarHeight(this)
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleUtils.applyLocale(newBase)
        super.attachBaseContext(newBase)
    }


}
