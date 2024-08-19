package com.alpermelkeli.cryptotrader

import android.app.Application
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage

class CryptoTraderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiStorage.initialize(applicationContext)
    }


}