package com.pvhung.ucar.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    fun getSpecificRiderDatabase(uid: String): DatabaseReference {
        return getRiderDatabase().child(uid)
    }

    fun getSpecificCustomerDatabase(uid: String): DatabaseReference {
        return getCustomerDatabase().child(uid)
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

    fun isUserAlreadyLogin(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

//    fun isNormalUser(): Boolean {
//        if (isUserAlreadyLogin()) {
//            val user = FirebaseAuth.getInstance().currentUser!
//            getSpecificCustomerDatabase(user.uid).va
//        }
//        throw
//    }
}