package com.alpermelkeli.cryptotrader.ui.LoginScreen

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.alpermelkeli.cryptotrader.databinding.ActivityLoginBinding
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (userViewModel.isLoggedIn()) {
            navigateToHomeScreen()
            finish()
        }

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.registerText.setOnClickListener {
            navigateToRegister()
        }

        userViewModel.authResult.observe(this) { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                navigateToHomeScreen()
                finish()
            } else {
                Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        userViewModel.loginUser(email, password)
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
