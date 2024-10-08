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

class FollowBotManager(
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
    openPosition: Boolean,
    var distanceInterval: Double,
    var followInterval: Double
) : BotManager(id, firstPairName, secondPairName, pairName, threshold, amount, exchangeMarket, status, apiKey, secretKey, openPosition) {

    private val binanceAccountOperations = BinanceAccountOperations(apiKey, secretKey)
    private val thresholdManager: ThresholdManager = ThresholdManager()
    private var webSocketManager: BinanceWebSocketManager? = null

    override fun start() {
        if (openPosition) {
            thresholdManager.setSellThreshold(pairName, threshold)
        } else {
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

    fun update(amount: Double, threshold: Double, distanceInterval: Double, followInterval: Double) {
        this.amount = amount
        this.threshold = threshold
        this.distanceInterval = distanceInterval
        this.followInterval = followInterval
        if (openPosition) {
            thresholdManager.setSellThreshold(pairName, threshold)
            thresholdManager.removeBuyThreshold(pairName)
        } else {
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

        if (currentPrice >= threshold + distanceInterval) {

            threshold += followInterval
            BotManagerStorage.updateFollowBotManager(id,this)
            if(openPosition){
                thresholdManager.setSellThreshold(pairName, threshold)
            }

            else{
                thresholdManager.setBuyThreshold(pairName, threshold)
            }
            BotService.sendNotification("Thresold Update","Threshold updated to $threshold due to price increase")
        }
        else if (currentPrice <= threshold - distanceInterval) {
            threshold -= followInterval
            BotManagerStorage.updateFollowBotManager(id,this)
            if(openPosition){
                thresholdManager.setSellThreshold(pairName,threshold)
            }
            else{
                thresholdManager.setBuyThreshold(pairName,threshold)
            }
            BotService.sendNotification("Thresold update","Threshold updated to $threshold due to price decrease")
        }

        if (!openPosition && buyThreshold != null && currentPrice > buyThreshold) {
            binanceAccountOperations.buyCoin(pairName, amount)
                .thenAccept { tradeResult: TradeResult ->
                    if (tradeResult.isSuccess) {
                        openPosition = true
                        BotManagerStorage.updateFollowBotManager(id, this)
                        thresholdManager.setSellThreshold(pairName, buyThreshold)
                        thresholdManager.removeBuyThreshold(pairName)
                        println("Total commission ${tradeResult.commission}")
                        BotService.sendNotification("Buy $id", "Buy order for $pairName executed successfully. Total commission: ${tradeResult.commission} $firstPairName")
                    } else {
                        BotService.sendNotification("Buy Failed $id", "Buy order for $pairName failed. Please check the logs for more details.")
                    }
                }
                .exceptionally { ex: Throwable ->
                    BotService.sendNotification("Buy Error $id", "An error occurred during buy operation for $pairName: ${ex.message}")
                    ex.printStackTrace()
                    null
                }
        }

        if (openPosition && sellThreshold != null && currentPrice < sellThreshold) {
            binanceAccountOperations.sellCoin(pairName, amount)
                .thenAccept { tradeResult: TradeResult ->
                    if (tradeResult.isSuccess) {
                        openPosition = false
                        BotManagerStorage.updateFollowBotManager(id, this)
                        thresholdManager.removeSellThreshold(pairName)
                        thresholdManager.setBuyThreshold(pairName, threshold)
                        println("Total commission ${tradeResult.commission}")
                        BotService.sendNotification("Sell Order $id", "Sell order for $pairName executed successfully. Total commission: ${tradeResult.commission} $secondPairName")
                    } else {
                        BotService.sendNotification("Sell Failed $id", "Sell order for $pairName failed. Please check the logs for more details.")
                    }
                }
                .exceptionally { ex: Throwable ->
                    BotService.sendNotification("Sell Error $id", "An error occurred during sell operation for $pairName: ${ex.message}")
                    ex.printStackTrace()
                    null
                }
        }
    }
}
