package com.pvhung.ucar.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.pvhung.ucar.ui.main.MainViewModel


abstract class BaseBindingDialogFragment<B : ViewDataBinding> :
    BaseDialogFragment() {
    lateinit var binding: B
    abstract val layoutId: Int
    lateinit var mainViewModel: MainViewModel
    private var loaded = false

    protected abstract fun onCreatedView(view: View?, savedInstanceState: Bundle?)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!loaded)
            binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.DialogFullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        if (!needToKeepView()) {
            onCreatedView(view, savedInstanceState)
        } else {
            if (!loaded) {
                onCreatedView(view, savedInstanceState)
                loaded = true
            }
        }
    }

    open fun needToKeepView(): Boolean {
        return false
    }

//    override fun onStart() {
//        super.onStart()
//        val dialog: Dialog? = dialog
//        if (dialog != null) {
//            val width: Int = ViewGroup.LayoutParams.MATCH_PARENT
//            val height: Int = ViewGroup.LayoutParams.MATCH_PARENT
//            dialog.window!!.setLayout(width, height)
//        }
//    }
}
