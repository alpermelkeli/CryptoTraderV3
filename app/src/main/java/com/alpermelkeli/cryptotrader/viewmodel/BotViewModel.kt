package com.alpermelkeli.cryptotrader.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alpermelkeli.cryptotrader.model.bot.PumpBotManager
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage

class BotViewModel: ViewModel() {
    private val _pumpBotManager : MutableLiveData<PumpBotManager> = MutableLiveData()
    val pumpBotManager : LiveData<PumpBotManager> get() = _pumpBotManager


    fun getPumpBotManager(){
        val pumpBotManager = BotManagerStorage.getPumpBotManager()
        pumpBotManager?.let {
            _pumpBotManager.postValue(it)
        }
    }
}