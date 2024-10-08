package com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase

data class FollowBotEntity(
    val id: String,
    val firstPairName: String,
    val secondPairName: String,
    val pairName: String,
    val threshold: Double,
    val amount: Double,
    val exchangeMarket: String,
    val status: String,
    val apiKey: String,
    val secretKey: String,
    val openPosition: Boolean,
    val distanceInterval:Double,
    val followInterval:Double
)
