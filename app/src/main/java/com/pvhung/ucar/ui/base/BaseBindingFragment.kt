package com.pvhung.ucar.ui.base


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.pvhung.ucar.R
import com.pvhung.ucar.ui.main.MainViewModel


abstract class BaseBindingFragment<B : ViewDataBinding, T : BaseViewModel> :
    BaseFragment() {
    private var toast: Toast? = null
    private var lastClickTime: Long = 0
    lateinit var binding: B
    lateinit var viewModel: T
    lateinit var mainViewModel: MainViewModel
    private var loaded = false
    protected abstract fun getViewModel(): Class<T>
    abstract val layoutId: Int

    protected var checkCountRate = false
    protected abstract fun onCreatedView(view: View?, savedInstanceState: Bundle?)
    protected abstract fun observerData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!loaded)
            binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (needInsetTop()) {
            var insectTop = 0
            if (requireActivity() is BaseActivity) {
                insectTop = (requireActivity() as BaseActivity).statusBarHeight
            }
            binding.root.setPadding(
                binding.root.paddingLeft, insectTop, binding.root.paddingRight,
                binding.root.paddingBottom
            )
        }

        viewModel = ViewModelProvider(this)[getViewModel()]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        if (!needToKeepView()) {
            onCreatedView(view, savedInstanceState)
        } else {
            if (!loaded) {
                onCreatedView(view, savedInstanceState)
                loaded = true
            }
        }
        observerData()
    }

    fun getHeightStatusBar(): Int {
        return (requireActivity() as BaseActivity).statusBarHeight
    }

    fun getHeightNavigationBar(): Int {
        return (requireActivity() as BaseActivity).navigationHeight
    }

    open fun needInsetTop(): Boolean {
        return false
    }


    open fun needToKeepView(): Boolean {
        return false
    }

    fun popBackStack() {
        findNavController().popBackStack()
    }

    fun popBackStack(id: Int, isInclusive: Boolean) {
        findNavController().popBackStack(id, isInclusive)
    }


    fun navigateScreen(bundle: Bundle?, id: Int) {
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)

        findNavController().navigate(id, bundle, navBuilder.build())
    }

    fun enableClickAlphaTopDrawable(view: TextView, isEnable: Boolean) {
        view.isClickable = isEnable
        view.isEnabled = isEnable
        if (isEnable) {
            view.alpha = 1f
            val birthdayDrawable: Array<Drawable> = view.compoundDrawables
            birthdayDrawable[1].alpha = 255
        } else {
            view.alpha = 0.7f
            val birthdayDrawable: Array<Drawable> = view.compoundDrawables
            birthdayDrawable[1].alpha = 100
        }
    }

    fun enableView(view: View, isEnable: Boolean) {
        view.isEnabled = isEnable
        view.isClickable = isEnable
    }

    open fun checkClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 700) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    open fun checkLongClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    open fun checkSoLongClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    open fun showToast(string: String) {
        toast?.cancel()
        toast = Toast.makeText(
            requireContext(),
            string,
            Toast.LENGTH_SHORT
        )
        toast!!.show()
    }

    override fun onStart() {
        super.onStart()
        lastClickTime = 0
    }

    open fun finishRate() {}

}