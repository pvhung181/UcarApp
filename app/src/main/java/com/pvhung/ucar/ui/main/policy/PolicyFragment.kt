package com.pvhung.ucar.ui.main.policy

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebViewClient
import com.pvhung.ucar.R
import com.pvhung.ucar.databinding.FragmentPolicyBinding
import com.pvhung.ucar.ui.base.BaseBindingDialogFragment

class PolicyFragment : BaseBindingDialogFragment<FragmentPolicyBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_policy

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl(getString(R.string.link_policy))

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = dialog.window!!.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MaterialDialogSheetPolicy)
    }

}