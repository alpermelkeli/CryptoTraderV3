package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.followbotfragment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.ActivityFollowBotDetailsBinding

class FollowBotDetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFollowBotDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowBotDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}