package com.alpermelkeli.cryptotrader.model.bot

import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceWebSocketManager
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceWebSocketManager.BinanceWebSocketListener
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.TradeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.internal.wait

class ManuelBotManager(
    id: String,
    firstPairName: String,
    secondPairName: String,
    pairName: String,
    threshold: Double,
    amount: Double,
    exchangeMarket: String,
    status: String,
    apiKey: String,
    secretKey: String,
    openPosition: Boolean
) : BotManager(id, firstPairName, secondPairName, pairName, threshold, amount, exchangeMarket, status, apiKey, secretKey, openPosition) {

    private val binanceAccountOperations = BinanceAccountOperations(apiKey,secretKey)
    private val thresholdManager: ThresholdManager = ThresholdManager()
    private var webSocketManager: BinanceWebSocketManager? = null

    override fun start() {
        if(openPosition){
            thresholdManager.setSellThreshold(pairName, threshold)
        }
        else{
            thresholdManager.setBuyThreshold(pairName, threshold)
        }

        val listener: BinanceWebSocketListener = object : BinanceWebSocketListener() {
            override fun onPriceUpdate(price: String) {
                val currentPrice = price.toDouble()
                handlePriceUpdate(currentPrice)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                BotService.sendNotification("Botun bağlantısı koptu!","Yeniden bağlantı deneniyor...")
                CoroutineScope(Dispatchers.Main).launch {
                    stop()
                    delay(10000)
                    start()
                }
            }
        }
        webSocketManager = BinanceWebSocketManager(listener)
        webSocketManager!!.connect(pairName)
    }
    fun update(amount: Double, threshold: Double) {
        this.amount = amount
        this.threshold = threshold

        if (openPosition) {
            thresholdManager.setSellThreshold(pairName, threshold)
            thresholdManager.removeBuyThreshold(pairName)
        }
        else {
            thresholdManager.setBuyThreshold(pairName, threshold)
            thresholdManager.removeSellThreshold(pairName)
        }
    }
    override fun stop() {
        webSocketManager?.disconnect()
        webSocketManager = null
    }
    override fun handlePriceUpdate(currentPrice: Double) {
        println("Bot id at botManager: $id")
        val buyThreshold = thresholdManager.getBuyThreshold(pairName)
        val sellThreshold = thresholdManager.getSellThreshold(pairName)

        println("Current price of $pairName = $currentPrice")
        println("Buy threshold of $pairName = $buyThreshold")
        println("Sell threshold of $pairName = $sellThreshold")
        println("Open position of $pairName = $openPosition")

        if (!openPosition && buyThreshold != null && currentPrice > buyThreshold) {

            binanceAccountOperations.buyCoin(pairName, amount)
                .thenAccept { tradeResult: TradeResult ->
                    if (tradeResult.isSuccess) {
                        println("Total comission ${tradeResult.commission}")
                        openPosition = true
                        BotManagerStorage.updateManuelBotManager(id, this)
                        thresholdManager.setSellThreshold(pairName, buyThreshold)
                        thresholdManager.removeBuyThreshold(pairName)
                        BotService.sendNotification("Buy $id", "Buy order for $pairName executed successfully. Total commission: ${tradeResult.commission} $firstPairName")

                    } else {
                        BotService.sendNotification("Buy Order Failed $id", "Buy order for $pairName failed. Please check the logs for more details.")
                    }
                }
                .exceptionally { ex: Throwable ->
                    BotService.sendNotification("Buy Order Error $id", "An error occurred during buy operation for $pairName: ${ex.message}")
                    ex.printStackTrace()
                    null
                }
        }

        if (openPosition && sellThreshold != null && currentPrice < sellThreshold) {
            binanceAccountOperations.sellCoin(pairName, amount)
                .thenAccept { tradeResult: TradeResult ->
                    if (tradeResult.isSuccess) {
                        openPosition = false
                        BotManagerStorage.updateManuelBotManager(id, this)
                        thresholdManager.removeSellThreshold(pairName)
                        thresholdManager.setBuyThreshold(pairName, threshold)
                        BotService.sendNotification("Sell Order $id", "Sell order for $pairName executed successfully. Total commission: ${tradeResult.commission} $secondPairName")
                    } else {
                        BotService.sendNotification("Sell Order Failed $id", "Sell order for $pairName failed. Please check the logs for more details.")
                    }
                }
                .exceptionally { ex: Throwable ->
                    BotService.sendNotification("Sell Order Error $id", "An error occurred during sell operation for $pairName: ${ex.message}")
                    ex.printStackTrace()
                    null
                }
        }
    }
}
