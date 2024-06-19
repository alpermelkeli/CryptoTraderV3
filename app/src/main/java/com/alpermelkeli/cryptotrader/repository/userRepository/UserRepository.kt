package com.alpermelkeli.cryptotrader.repository.userRepository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class UserRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun registerUser(email: String, password: String, accountType: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveLoginState(true)
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveLoginState(true)
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
        saveLoginState(false)
    }

    fun getCurrentUser() = auth.currentUser

    private fun saveLoginState(isLoggedIn: Boolean) {
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
}
