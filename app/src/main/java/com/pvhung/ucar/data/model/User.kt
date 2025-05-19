package com.pvhung.ucar.data.model

data class User(
    var fullName: String = "",
    var phoneNumber: String = "",
    var email: String = "",
    var gender: String = "",
    var dateOfBirth: String = "",
    var password: String = "",
    var rating: Float = 5f,
    var isDriver: Boolean = false,
    var isActive: Boolean = true,
    var numberPlate: String = "",
    var avatar: String = ""
) {
    private var service = ""
    fun setService(s: String) {
        if (isDriver) service = s
    }
    fun getService(): String {
        return service
    }
//    fun convertPropertiesToMap(): Map<String, Any> {
//        val map = mutableMapOf<String, Any>()
//        map[Constant.KEY_FULL_NAME] = fullName
//        map[Constant.KEY_PHONE_NUMBER] = phoneNumber
//        map[Constant.KEY_EMAIL] = email
//        map[Constant.KEY_GENDER] = gender
//        map[Constant.KEY_DATE_OF_BIRTH] = dateOfBirth
//        map[Constant.KEY_PASSWORD] = password
//
//        return map
//    }
}