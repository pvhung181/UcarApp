package com.pvhung.ucar.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pvhung.ucar.data.model.User
import com.pvhung.ucar.ui.base.BaseViewModel
import com.pvhung.ucar.utils.FirebaseDatabaseUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {
    val user = MutableLiveData<User?>(null)
    val isError = MutableLiveData<Boolean?>(null)

    fun getUserFromServer() {
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
            isError.value = true
        }) {
            FirebaseDatabaseUtils.getSpecificCustomerDatabase(FirebaseAuth.getInstance().currentUser?.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            user.postValue(FirebaseDatabaseUtils.getUserFromSnapshot(snapshot))
                        } else {
                            FirebaseDatabaseUtils.getSpecificCustomerDatabase(FirebaseAuth.getInstance().currentUser?.uid!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        user.postValue(
                                            FirebaseDatabaseUtils.getUserFromSnapshot(
                                                snapshot
                                            )
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        isError.postValue(true)
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isError.postValue(true)
                    }
                })
        }
    }
}