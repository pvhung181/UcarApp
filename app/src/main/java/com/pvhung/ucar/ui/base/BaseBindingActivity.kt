package com.pvhung.ucar.ui.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation

abstract class BaseBindingActivity<B : ViewDataBinding, VM : BaseViewModel> :
    BaseActivity() {
    lateinit var binding: B
    lateinit var viewModel: VM
    abstract val layoutId: Int
    private var lastClickTime: Long = 0
    private var toast: Toast? = null

    abstract fun getViewModel(): Class<VM>
    abstract fun setupView(savedInstanceState: Bundle?)
    abstract fun setupData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        viewModel = ViewModelProvider(this)[getViewModel()]
        changeSystemUIColor(Color.TRANSPARENT, Color.TRANSPARENT, false, false, true)
        setupView(savedInstanceState)
        setupData()
    }

    fun changeSystemUIColor(
        colorStatusBar: Int,
        colorNavigationBar: Int,
        darkStatusBar: Boolean,
        darkNavigation: Boolean,
        fullScreen: Boolean
    ) {
        val systemUiScrim = Color.parseColor("#40000000")
        var systemUiVisibility = 0
        val winParams = window.attributes
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (darkNavigation) {
                window.navigationBarColor = colorNavigationBar
            } else {
                window.navigationBarColor = systemUiScrim
            }
            if (darkStatusBar) {
                window.statusBarColor = colorStatusBar
            } else {
                window.statusBarColor = systemUiScrim
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!darkStatusBar) {
                systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.statusBarColor = colorStatusBar
            if (darkNavigation) {
                window.navigationBarColor = colorNavigationBar
            } else {
                window.navigationBarColor = systemUiScrim
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!darkNavigation) {
                systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.navigationBarColor = colorNavigationBar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    window.isNavigationBarContrastEnforced = false
                } catch (ignored: NoSuchMethodError) {
                }
            }
        }
        systemUiVisibility = if (!fullScreen) {
            systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        window.decorView.systemUiVisibility = systemUiVisibility
        window.attributes = winParams
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    open fun navigateScreen(bundle: Bundle?, view: View?, id: Int) {
        val navBuilder = NavOptions.Builder()
        Navigation.findNavController(view!!)
            .navigate(id, bundle, navBuilder.build())
    }

    open fun checkLongClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    open fun checkClickTime(time: Int): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < time) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onStart() {
        super.onStart()
        lastClickTime = 0
    }

    open fun showToast(string: String) {
        toast?.cancel()
        toast = Toast.makeText(
            this,
            string,
            Toast.LENGTH_SHORT
        )
        toast!!.show()
    }

    open fun nextScreenOpen() {}
    open fun onLoadAdsOpen() {}
}