package com.pvhung.ucar

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.data.local.SharedPreferenceHelper
import com.pvhung.ucar.utils.LocaleUtils
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var mPrefHelper: SharedPreferenceHelper

    companion object {
        @JvmStatic
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        var newUsing = mPrefHelper.getInt(Constant.NEW_USING)
        if (newUsing == 0) {
            newUsing = 1
            mPrefHelper.storeInt(Constant.NEW_USING, newUsing)
            for (i in LocaleUtils.getLanguages().indices) {
                val newLocale = Locale(LocaleUtils.getLanguages()[i].codeLocale)
                if (newLocale.language.contains(LocaleUtils.getLocaleCompat(baseContext.resources).language)) {
                    mPrefHelper.storeString(
                        Constant.PREF_SETTING_LANGUAGE, LocaleUtils.getLanguages(
                        )[i].codeLocale
                    )
                    break
                }
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.applyLocale(this)
    }
}
