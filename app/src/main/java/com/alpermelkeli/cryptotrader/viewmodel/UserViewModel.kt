package com.alpermelkeli.cryptotrader.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alpermelkeli.cryptotrader.repository.userRepository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(context: Context) : ViewModel() {

    private val userRepository: UserRepository = UserRepository(context)

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    private val _authResult = MutableLiveData<Pair<Boolean, String?>>()
    val authResult: LiveData<Pair<Boolean, String?>> get() = _authResult

    private val _userDocument = MutableLiveData<Map<String, Any>?>()
    val userDocument: LiveData<Map<String, Any>?> get() = _userDocument

    fun getTempBalance():Float{
        return userRepository.getTempBalance()
    }
    fun storeTempBalance(balance:Double){
        userRepository.storeTempBalance(balance)
    }

    fun registerUser(email: String, password: String, accountType: String, name:String, surname:String, phoneNumber:String) {
        userRepository.registerUser(email, password, accountType, name, surname, phoneNumber) { success, message ->
            _authResult.postValue(Pair(success, message))
            if (success) {
                _user.postValue(userRepository.getCurrentUser())
            }
        }
    }
    fun sendPasswordResetEmail(email: String) {
        userRepository.sendPasswordResetEmail(email) { success, message ->
            _authResult.postValue(Pair(success, message))
        }
    }


    fun loginUser(email: String, password: String) {
        userRepository.loginUser(email, password) { success, message ->
            _authResult.postValue(Pair(success, message))
            if (success) {
                _user.postValue(userRepository.getCurrentUser())
            }
        }
    }

    fun logout() {
        userRepository.logout()
        _user.postValue(null)
    }

    fun getCurrentUser() {
        _user.postValue(userRepository.getCurrentUser())
    }

    fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }

    fun fetchUserDocument() {
        userRepository.getUserDocument { success, message, data ->
            if (success) {
                _userDocument.postValue(data)
            }
        }
    }
}
