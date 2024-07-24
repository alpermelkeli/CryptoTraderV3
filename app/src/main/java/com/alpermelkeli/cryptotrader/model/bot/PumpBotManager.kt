package com.alpermelkeli.cryptotrader.model.bot

import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinancePumpDetection
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.KlineListener

class PumpBotManager(
    var pair: String,
    var openPosition: Boolean,
    var limit:Double,
    var percent:Int,
    var active:Boolean,
    var interval:String
) : KlineListener {

    private val binancePumpDetection = BinancePumpDetection()
    private var openPrice = 0.0
    private val binanceAccountOperations = BinanceAccountOperations(ApiStorage.getSelectedApi()?.apiKey,ApiStorage.getSelectedApi()?.secretKey)

    init {
        binancePumpDetection.setKlineListener(this)
    }


    fun start() {
        binancePumpDetection.startWebSocketConnection()
    }

    fun update(limit: Double,percent: Int,interval: String){
        this.limit = limit
        this.percent = percent
        this.interval = interval
    }
    fun stop(){
        binancePumpDetection.stopWebSocketConnection()
    }

    override fun onKlineProcessed(symbol: String, percentChange: Double, closePrice:Double) {
        if (percentChange > limit && !openPosition) {
            BotService.sendNotification("PumpBot","$symbol had a pump of $percentChange openPrice is $closePrice")
            openPrice = closePrice
            openPosition = true
            BotManagerStorage.updatePumpBotManager(this)
        }
    }
}
