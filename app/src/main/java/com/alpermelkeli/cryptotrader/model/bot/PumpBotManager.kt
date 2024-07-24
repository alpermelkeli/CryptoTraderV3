package com.alpermelkeli.cryptotrader.model.bot

import android.util.Log
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinancePumpDetection
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceWebSocketManager
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceWebSocketManager.BinanceWebSocketListener
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.KlineListener
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.TradeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket

class PumpBotManager(
    var pair: String,
    var openPosition: Boolean,
    var limit:Double,
    var amount:Double,
    var active:Boolean,
    var interval:String
) : KlineListener {
    private @Volatile var processingOrder = false
    private val binancePumpDetection = BinancePumpDetection()
    private var openPrice:Double = 0.0
    private val binanceAccountOperations = BinanceAccountOperations(ApiStorage.getSelectedApi()?.apiKey,ApiStorage.getSelectedApi()?.secretKey)
    private var webSocketManager: BinanceWebSocketManager? = null

    init {
        binancePumpDetection.setKlineListener(this)
    }


    fun start() {
        binancePumpDetection.startWebSocketConnection()
    }

    fun update(limit: Double,amount: Double,interval: String){
        this.limit = limit
        this.amount = amount
        this.interval = interval
    }
    fun stop(){
        binancePumpDetection.stopWebSocketConnection()
        untrackOpenedPosition()
    }

    private fun trackOpenedPosition(symbol: String){
        binancePumpDetection.stopWebSocketConnection()
        val listener: BinanceWebSocketListener = object : BinanceWebSocketListener() {
            override fun onPriceUpdate(price: String) {
                val currentPrice = price.toDouble()
                handlePriceUpdate(currentPrice)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                BotService.sendNotification("Pump botun açık pozisyon bağlantısı koptu!","Yeniden bağlantı deneniyor...")
                CoroutineScope(Dispatchers.Main).launch {
                    untrackOpenedPosition()
                    delay(10000)
                    trackOpenedPosition(pair)
                }
            }
        }
        webSocketManager = BinanceWebSocketManager(listener)
        webSocketManager!!.connect(symbol)
    }

    private fun untrackOpenedPosition(){
        webSocketManager?.disconnect()
        webSocketManager = null
        openPosition = false
        BotManagerStorage.updatePumpBotManager(this)
    }

    override fun onKlineProcessed(symbol: String, percentChange: Double, closePrice:Double) {
        Log.d("Pump Bot", "$limit $interval")
        if (percentChange > limit && !openPosition && !processingOrder) {
            BotService.sendNotification("PumpBot","$symbol had a pump of $percentChange openPrice is $closePrice")
            processingOrder = true

            binanceAccountOperations.buyCoinWithUSDT(symbol, amount)
                .thenAccept { tradeResult: TradeResult ->
                    if (tradeResult.isSuccess) {
                        openPrice = closePrice
                        openPosition = true
                        pair = symbol
                        BotManagerStorage.updatePumpBotManager(this)
                        BotService.sendNotification("Buy pump", "Buy order for $symbol executed successfully. Total commission: ${tradeResult.commission} $symbol")
                        stop()
                    //trackOpenedPosition(symbol)

                    } else {
                        BotService.sendNotification("Buy Order Failed Pump", "Buy order for $symbol failed. Please check the logs for more details.")
                    }
                }
                .exceptionally { ex: Throwable ->
                    BotService.sendNotification("Buy Order Error Pump", "An error occurred during buy operation for $symbol: ${ex.message}")
                    ex.printStackTrace()
                    null
                }
                .whenComplete { _, _ -> processingOrder = false }

        }
    }

    fun handlePriceUpdate(currentPrice: Double) {
        val percent = ((currentPrice-openPrice)/openPrice)*100
        Log.d("Websocket", "$currentPrice currentPrice currentPercent $percent")
        if(percent<-0.05 && !processingOrder){
            BotService.sendNotification("Pump Bot", "Yüzdelik -0.05 in altına düştü satıldı.")
            binanceAccountOperations.sellCoinWithUSDT(pair,amount)
                .thenAccept{tradeResult:TradeResult ->
                    if(tradeResult.isSuccess){
                        openPosition = false
                        pair = "PairName"
                        untrackOpenedPosition()
                        BotManagerStorage.updatePumpBotManager(this)
                        BotService.sendNotification("Sell pump", "Buy order for $pair executed successfully. Total commission: ${tradeResult.commission} $pair")
                    }
                    else{
                        BotService.sendNotification("Sell Order Failed Pump", "Buy order for $pair failed. Please check the logs for more details.")
                    }
                }
                .exceptionally {
                        ex:Throwable->
                    BotService.sendNotification("Sell Order Error Pump", "An error occurred during buy operation for $pair: ${ex.message}")
                    null
                }
                .whenComplete { _, _ -> processingOrder = false }

            start()
        }
        else if (percent>20.0 && !processingOrder){
            BotService.sendNotification("Pump Bot","Yüzdelik 20 un üstüne çıktı satılma.")
            binanceAccountOperations.sellCoin(pair,amount)
                .thenAccept{tradeResult:TradeResult ->
                    if(tradeResult.isSuccess){
                        openPosition = false
                        pair = "PairName"
                        untrackOpenedPosition()
                        BotManagerStorage.updatePumpBotManager(this)
                        BotService.sendNotification("Sell pump", "Buy order for $pair executed successfully. Total commission: ${tradeResult.commission} $pair")
                    }
                    else{
                        BotService.sendNotification("Sell Order Failed Pump", "Buy order for $pair failed. Please check the logs for more details.")
                    }
                }
                .exceptionally {
                        ex:Throwable->
                    BotService.sendNotification("Sell Order Error Pump", "An error occurred during buy operation for $pair: ${ex.message}")
                    null
                }
                .whenComplete { _, _ -> processingOrder = false }

            start()
        }
    }

}
