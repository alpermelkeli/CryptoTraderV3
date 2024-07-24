package com.alpermelkeli.cryptotrader.model.bot

import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinancePumpDetection
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.KlineListener

class PumpBotManager(
    var pair: String,
    var openPosition: Boolean,
    var enterPrice: Double
) : KlineListener {
    private val binancePumpDetection = BinancePumpDetection()

    init {
        binancePumpDetection.setKlineListener(this)
    }

    fun start() {
        binancePumpDetection.startWebSocketConnection()
    }

    override fun onKlineProcessed(symbol: String, percentChange: Double,closePrice:Double) {
        if (percentChange > 0.1) {
            println("$symbol had a pump of $percentChange openPrice is $closePrice")
            enterPrice = closePrice
            openPosition = true
        }
    }
}
