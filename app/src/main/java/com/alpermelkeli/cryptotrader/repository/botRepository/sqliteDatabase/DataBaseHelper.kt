package com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase

interface DataBaseHelper<T> {
    fun insertBot(bot: T)
    fun getAllBots(): List<T>
    fun getBotById(id: String): T?
    fun removeBotById(id: String)
    fun deleteAllBots()
}