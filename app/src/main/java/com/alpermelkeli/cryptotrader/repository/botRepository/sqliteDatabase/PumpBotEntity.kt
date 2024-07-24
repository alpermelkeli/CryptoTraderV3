package com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase

data class PumpBotEntity(var limit:Double, var openPosition:Boolean, var pairName:String, var percent:Int,
    var active:Boolean,var interval:String)
