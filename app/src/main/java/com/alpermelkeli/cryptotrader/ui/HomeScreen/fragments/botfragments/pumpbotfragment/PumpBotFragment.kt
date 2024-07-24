package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.pumpbotfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpermelkeli.cryptotrader.databinding.FragmentPumpBotBinding
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
            updatePumpBot(binding.limitEditText.text.toString().toDouble(),binding.amountEditText.text.toString().toDouble(),binding.intervalEditText.text.toString())
        }
        binding.passiveButton.setOnClickListener {
            stopPumpBot()
        }

        return binding.root
    }
    private fun retrieveBotInformation(){
        val pumpBotManager = BotManagerStorage.getPumpBotManager()
        pumpBotManager?.let {
            binding.pairText.text = it?.pair
            binding.openPositionText.text = if(it?.openPosition!!) "Posizyon Açık" else "Pozisyon Kapalı"
            binding.activeText.text = if(it?.active!!)"Bot Aktif" else "Bot Pasif"
            binding.amountEditText.setText(it.amount.toString())
            binding.intervalEditText.setText(it.interval)
            binding.limitEditText.setText(it.limit.toString())
        }

    }

    private fun stopPumpBot(){
        BotService.stopPumpBot()
    }
    private fun updatePumpBot(
                               limit: Double,
                               amount: Double,
                               interval: String){
        BotService.updatePumpBot(limit, amount, interval)
    }
}