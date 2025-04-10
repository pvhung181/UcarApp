package com.pvhung.ucar.utils

import com.pvhung.ucar.common.enums.SettingType

object DataProvider {
    fun getSettingItems(): List<SettingType> {
        return listOf(
            SettingType.SIGN_OUT
        )
    }
}