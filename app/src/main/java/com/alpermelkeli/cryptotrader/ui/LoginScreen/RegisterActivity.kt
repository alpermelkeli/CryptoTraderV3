package com.alpermelkeli.cryptotrader.ui.LoginScreen

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.ActivityLoginBinding
import com.alpermelkeli.cryptotrader.databinding.ActivityRegisterBinding
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            register()
        }

        // Observe auth result live data
        userViewModel.authResult.observe(this, Observer { result ->
            if (result.first) {
                Toast.makeText(this, "Kayıt işlemi başarılı! E-postanızı onaylayıp giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(this, result.second ?: "Bir hata oluştu!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun register() {
        val email =  binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val passwordConfirm = binding.passwordConfirmEditText.text.toString()
        val accountType = "Trade"
        val name = binding.nameEditText.text.toString()
        val surname = binding.surnameEditText.text.toString()
        val phoneNumber = binding.phoneNumberEditText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && surname.isNotEmpty() && phoneNumber.isNotEmpty()){

            if(passwordConfirm == password) userViewModel.registerUser(email, password, accountType, name, surname, phoneNumber)

            else Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show()
        }
        else Toast.makeText(this, "Hiçbir alan boş bırakılamaz", Toast.LENGTH_SHORT).show()

    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
