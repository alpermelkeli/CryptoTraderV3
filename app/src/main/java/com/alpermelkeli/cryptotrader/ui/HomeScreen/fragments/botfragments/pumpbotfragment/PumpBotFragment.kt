package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.pumpbotfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.model.bot.PumpBotManager


class PumpBotFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val manager = PumpBotManager("a",false,100.0)
        manager.start()

        return inflater.inflate(R.layout.fragment_pump_bot, container, false)
    }


}