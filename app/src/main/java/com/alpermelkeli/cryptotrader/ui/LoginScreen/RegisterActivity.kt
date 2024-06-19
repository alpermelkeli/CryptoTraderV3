package com.alpermelkeli.cryptotrader.ui.LoginScreen

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.ActivityLoginBinding
import com.alpermelkeli.cryptotrader.databinding.ActivityRegisterBinding
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            userViewModel.registerUser(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(), "Trade")
        }

    }
    private fun register(){

    }


}