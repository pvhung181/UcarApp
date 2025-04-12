package com.pvhung.ucar.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pvhung.ucar.common.Constant
import com.pvhung.ucar.data.model.User

object FirebaseDatabaseUtils {
    fun getRiderDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.USERS_REFERENCES)
            .child(Constant.RIDERS_REFERENCES)
    }

    fun getCustomerDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.USERS_REFERENCES)
            .child(Constant.CUSTOMERS_REFERENCES)
    }

    fun getDriverAvailableDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.USERS_REFERENCES)
            .child(Constant.DRIVERS_AVAILABLE_REFERENCES)
    }

    fun saveUserInfo(uid: String, user: User): Boolean {
        val dbRef =
            if (user.isDriver) getRiderDatabase().child(uid) else getCustomerDatabase().child(uid)
        dbRef.setValue(user).addOnSuccessListener {

        }
            .addOnFailureListener {

            }
        return false
    }
}