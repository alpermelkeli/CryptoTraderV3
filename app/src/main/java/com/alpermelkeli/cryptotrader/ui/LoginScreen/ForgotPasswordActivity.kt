package com.alpermelkeli.cryptotrader.ui.LoginScreen

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.ActivityForgotPasswordBinding
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(applicationContext)
    }
    private lateinit var binding : ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendLinkButton.setOnClickListener {
            sendPasswordReset(binding.forgotEmailEditText.text.toString())
        }


    }

    private fun sendPasswordReset(email:String){
        userViewModel.sendPasswordResetEmail(email)
        Toast.makeText(applicationContext, "E-postan doğru ise şifre yenileme linki gönderildi.", Toast.LENGTH_SHORT).show()
        finish()
    }


}