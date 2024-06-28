package com.alpermelkeli.cryptotrader.model.bot

abstract class BotManager(
    val id: String,
    var firstPairName: String,
    var secondPairName: String,
    var pairName: String,
    var threshold: Double,
    var amount: Double,
    var exchangeMarket: String,
    var status: String,
    var apiKey: String,
    var secretKey: String,
    var openPosition: Boolean
)
{
    abstract fun start()
    abstract fun stop()
    abstract fun handlePriceUpdate(currentPrice:Double)
}