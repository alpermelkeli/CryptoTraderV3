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
    private val sharedPref = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    fun storeTempBalance(balance:Double){
        val editor = sharedPref.edit()
        editor.putFloat("tempBalance", balance.toFloat())
        editor.apply()
    }
    fun getTempBalance():Float{
        val balance = sharedPref.getFloat("tempBalance",0f)
        return balance
    }

    fun registerUser(email: String, password: String, accountType: String, name:String, surname:String, phoneNumber:String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser: FirebaseUser? = auth.currentUser
                firebaseUser?.let { user ->
                    val userModel = User(email, accountType, name, surname, phoneNumber)
                    firestore.collection("users").document(user.uid)
                        .set(userModel)
                        .addOnSuccessListener {
                            user.sendEmailVerification().addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    callback(true, "Verification email sent")
                                } else {
                                    callback(false, emailTask.exception?.message)
                                }
                            }
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
                val firebaseUser: FirebaseUser? = auth.currentUser
                if (firebaseUser != null && firebaseUser.isEmailVerified) {
                    callback(true, null)
                } else {
                    auth.signOut()
                    callback(false, "Email is not verified. Please verify your email first.")
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(email: String, callback: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Password reset email sent")
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun isLoggedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null && currentUser.isEmailVerified
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
