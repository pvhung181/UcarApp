package com.pvhung.ucar.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
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

    fun getSpecificRiderDatabase(uid: String): DatabaseReference {
        return getRiderDatabase().child(uid)
    }

    fun getSpecificCustomerDatabase(uid: String): DatabaseReference {
        return getCustomerDatabase().child(uid)
    }

    fun getDriverAvailableDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference()
            .child(Constant.DRIVERS_AVAILABLE_REFERENCES)
    }

    fun getDriverWorkingDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference()
            .child(Constant.DRIVERS_WORKING_REFERENCES)
    }

    fun getRequestsDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.CUSTOMER_REQUESTS_REFERENCES)
    }

    fun getCurrentCustomerDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.USERS_REFERENCES)
            .child(Constant.CUSTOMERS_REFERENCES)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    fun getCurrentDriverDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(Constant.USERS_REFERENCES)
            .child(Constant.RIDERS_REFERENCES).child(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    fun getHistoryDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference()
            .child(Constant.HISTORY_REFERENCES)
    }


    fun getCustomerHistoryDatabase(uid: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference()
            .child(Constant.USERS_REFERENCES).child(Constant.CUSTOMERS_REFERENCES).child(uid)
            .child(Constant.HISTORY_REFERENCES)
    }

    fun getDriverHistoryDatabase(uid: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference()
            .child(Constant.USERS_REFERENCES).child(Constant.RIDERS_REFERENCES).child(uid)
            .child(Constant.HISTORY_REFERENCES)
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

    fun getUserFromSnapshot(snapshot: DataSnapshot): User? {
        try {
            val user = User().apply {
                fullName = snapshot.child(Constant.USER_NAME).getStringValue()
                phoneNumber = snapshot.child(Constant.USER_PHONE).getStringValue()
                email = snapshot.child(Constant.USER_EMAIL).getStringValue()
                gender = snapshot.child(Constant.USER_GENDER).getStringValue()
                dateOfBirth = snapshot.child(Constant.USER_BIRTH).getStringValue()
                rating = snapshot.child(Constant.USER_RATING).getNumberValue()
                isDriver = snapshot.child(Constant.USER_IS_DRIVER).getBooleanValue()
                isActive = snapshot.child(Constant.USER_IS_ACTIVE).getBooleanValue()
                numberPlate = snapshot.child(Constant.USER_NUMBER_PLATE).getStringValue()
                avatar = snapshot.child(Constant.USER_AVATAR).getStringValue()
                setService(snapshot.child(Constant.USER_SERVICE).getStringValue())
            }
            return user
        } catch (_: Exception) {
            return null
        }
    }

    fun removeDriverAvailable() {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            getDriverAvailableDatabase().child(it).removeValue()
        }
    }
}