package com.pvhung.ucar.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.data.model.User

object Utils {
    fun getUid(callback: (String) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            callback.invoke(it)
        }
    }

    fun removeRequest() {
        getUid {
            FirebaseDatabaseUtils.getRequestsDatabase().child(it).removeValue()
        }
    }

    fun removeDriverWorking() {
        getUid {
            FirebaseDatabaseUtils.getDriverWorkingDatabase().child(it).removeValue()
        }
    }

    fun removeDriverAvailable() {
        getUid {
            FirebaseDatabaseUtils.getDriverAvailableDatabase().child(it).removeValue()
        }
    }
}