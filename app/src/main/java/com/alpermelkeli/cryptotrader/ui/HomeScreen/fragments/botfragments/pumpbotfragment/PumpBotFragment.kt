package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.pumpbotfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentPumpBotBinding
import com.alpermelkeli.cryptotrader.model.bot.PumpBotManager
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage


class PumpBotFragment : Fragment() {
    private lateinit var binding: FragmentPumpBotBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPumpBotBinding.inflate(inflater,container,false)
        retrieveBotInformation()
        binding.updateButton.setOnClickListener {
            updatePumpBot(binding.limitEditText.text.toString().toDouble(),binding.percentEditText.text.toString().toInt(),binding.intervalEditText.text.toString())
        }
        binding.passiveButton.setOnClickListener {
            stopPumpBot()
        }




        return binding.root
    }
    private fun retrieveBotInformation(){
        val pumpBotManager = BotManagerStorage.getPumpBotManager()
        pumpBotManager.let {
            binding.pairText.text = it?.pair
            binding.openPositionText.text = it?.openPosition.toString()
            binding.activeText.text = it?.active.toString()
        }

    }




    private fun stopPumpBot(){
        BotService.stopPumpBot()
    }
    private fun updatePumpBot(
                               limit: Double,
                               percent: Int,
                               interval: String){
        BotService.updatePumpBot(limit, percent, interval)
    }
}