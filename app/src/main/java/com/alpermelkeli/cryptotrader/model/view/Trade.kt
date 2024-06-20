package com.alpermelkeli.cryptotrader.model.view

data class Trade(
    val time:String,
    val price:Double,
    val amount:Double,
    val isBuyer:Boolean
)
