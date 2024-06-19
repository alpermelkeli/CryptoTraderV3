package com.alpermelkeli.cryptotrader.repository.userRepository

import android.content.Context
import android.content.SharedPreferences
import com.alpermelkeli.cryptotrader.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun registerUser(email: String, password: String, accountType: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser: FirebaseUser? = auth.currentUser
                firebaseUser?.let {
                    val user = User(email, "", accountType)
                    firestore.collection("users").document(it.uid)
                        .set(user)
                        .addOnSuccessListener {
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message)
                        }
                } ?: callback(false, "User registration failed")
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

    fun getUserDocument(callback: (Boolean, String?, Map<String, Any>?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        callback(true, null, document.data)
                    } else {
                        callback(false, "Document does not exist", null)
                    }
                } else {
                    callback(false, task.exception?.message, null)
                }
            }
        } else {
            callback(false, "No current user", null)
        }
    }
}
