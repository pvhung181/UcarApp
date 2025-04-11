package com.pvhung.ucar.interfaces

import com.pvhung.ucar.common.enums.SettingType

interface OnSettingListener {
    fun onItemClick(type: SettingType)
}