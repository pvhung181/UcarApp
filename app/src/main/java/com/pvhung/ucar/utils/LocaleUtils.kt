package com.pvhung.ucar.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import android.text.TextUtils
import com.pvhung.ucar.App
import com.pvhung.ucar.R
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.common.model.Language
import com.pvhung.ucar.common.model.ListLanguage
import java.util.Locale


object LocaleUtils {
    var codeLanguageCurrent: String? = Constant.LANGUAGE_EN

    fun applyLocale(context: Context) {
        val preferences = PreferenceManager
            .getDefaultSharedPreferences(context)
        codeLanguageCurrent =
            preferences.getString(Constant.PREF_SETTING_LANGUAGE, Constant.LANGUAGE_EN)
        if (TextUtils.isEmpty(codeLanguageCurrent)) {
            codeLanguageCurrent = Constant.LANGUAGE_EN
        }
        val newLocale = Locale(codeLanguageCurrent.toString())
        updateResource(context, newLocale)
        if (context !== context.applicationContext) {
            updateResource(context.applicationContext, newLocale)
        }
    }

    fun setLocal(local: String, activity: Context): Context {
        var context: Context = activity
        val locale = Locale(local)
        Locale.setDefault(locale)

        val res: Resources = context.resources
        val config = Configuration(res.configuration)
        config.locale = locale
        context = context.createConfigurationContext(config)

        return context
    }

    private fun updateResource(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources = context.resources
        val current = getLocaleCompat(resources)
        if (current === locale) {
            return
        }
        val configuration = Configuration(resources.configuration)
        if (isAtLeastSdkVersion(Build.VERSION_CODES.N)) {
            configuration.setLocale(locale)
        } else if (isAtLeastSdkVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun getLocaleCompat(resources: Resources): Locale {
        val configuration = resources.configuration
        return if (isAtLeastSdkVersion(Build.VERSION_CODES.N)) configuration.locales[0] else configuration.locale
    }

    private fun isAtLeastSdkVersion(versionCode: Int): Boolean {
        return Build.VERSION.SDK_INT >= versionCode
    }

//    fun applyLocaleAndRestart(activity: Activity, localeString: String) {
//        val preferences = PreferenceManager
//            .getDefaultSharedPreferences(activity)
//        preferences.edit().putString(Constant.PREF_SETTING_LANGUAGE, localeString).apply()
//        applyLocale(activity)
//
//        val intent = Intent(activity, SettingActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        activity.startActivity(intent)
//        ActivityCompat.finishAffinity(activity)
//    }
//
//    fun applyLocaleAndRestartFirst(activity: Activity, localeString: String) {
//        val preferences = PreferenceManager
//            .getDefaultSharedPreferences(activity)
//        preferences.edit().putString(Constant.PREF_SETTING_LANGUAGE, localeString).apply()
//        applyLocale(activity)
//
//        val intent = Intent(activity, SettingActivity::class.java)
//        intent.putExtra(Constant.FIRST_ON_BOARD,true)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        activity.startActivity(intent)
//        ActivityCompat.finishAffinity(activity)
//    }

    fun applyLocalInFirst(activity: Activity, localeString: String?) {
        val preferences = PreferenceManager
            .getDefaultSharedPreferences(activity)
        preferences.edit().putString(Constant.PREF_SETTING_LANGUAGE, localeString).apply()
        applyLocale(activity)
    }

    fun getLanguages(): List<Language> {
        val list: MutableList<Language> = ArrayList()
        list.add(Language(Constant.LANGUAGE_VN, App.instance.resources.getString(R.string.vietnam)))
        list.add(Language(Constant.LANGUAGE_EN, App.instance.resources.getString(R.string.english)))

        return list
    }

    fun getListLanguage(): ListLanguage {
        val list: List<Language> = getLanguages()
        var posSelected = 0
        for (i in list.indices) {
            if (list[i].codeLocale == codeLanguageCurrent
            ) {
                posSelected = i
                break
            }
        }
        return ListLanguage(list, posSelected)
    }

    val Context.preferredLocale: Locale
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
}
