package com.alpermelkeli.cryptotrader.repository.botRepository.ram

import android.content.Context
import com.alpermelkeli.cryptotrader.model.FollowBotManager
import com.alpermelkeli.cryptotrader.model.ManuelBotManager
import com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase.FollowBotDatabaseHelper
import com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase.FollowBotEntity
import com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase.ManuelBotDatabaseHelper
import com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase.ManuelBotEntity

object BotManagerStorage {

    private val manuelBotManagers: MutableMap<String, ManuelBotManager> = mutableMapOf()

    private val followBotManagers: MutableMap<String, FollowBotManager> = mutableMapOf()

    private lateinit var manuelBotDbHelper: ManuelBotDatabaseHelper

    private lateinit var followBotDbHelper: FollowBotDatabaseHelper

    fun initialize(context: Context) {
        manuelBotDbHelper = ManuelBotDatabaseHelper(context)
        followBotDbHelper = FollowBotDatabaseHelper(context)
        loadManuelBotsFromDatabase()
        loadFollowBotsFromDatabase()
    }

    fun addManuelBotManager(manuelBotManager: ManuelBotManager) {
        manuelBotManagers[manuelBotManager.id] = manuelBotManager
        manuelBotDbHelper.insertBot(
            ManuelBotEntity(
                manuelBotManager.id,
                manuelBotManager.firstPairName,
                manuelBotManager.secondPairName,
                manuelBotManager.pairName,
                manuelBotManager.threshold,
                manuelBotManager.amount,
                manuelBotManager.exchangeMarket,
                manuelBotManager.status,
                manuelBotManager.apiKey,
                manuelBotManager.secretKey,
                manuelBotManager.openPosition
            )
        )
    }

    fun addFollowBotManager(followBotManager: FollowBotManager) {
        followBotManagers[followBotManager.id] = followBotManager
        followBotDbHelper.insertBot(
            FollowBotEntity(
                followBotManager.id,
                followBotManager.firstPairName,
                followBotManager.secondPairName,
                followBotManager.pairName,
                followBotManager.threshold,
                followBotManager.amount,
                followBotManager.exchangeMarket,
                followBotManager.status,
                followBotManager.apiKey,
                followBotManager.secretKey,
                followBotManager.openPosition,
                followBotManager.distanceInterval,
                followBotManager.followInterval
            )
        )

    }

    fun getManuelBotManager(id: String): ManuelBotManager? {
        return manuelBotManagers[id]
    }

    fun getFollowBotManager(id: String): FollowBotManager? {
        return followBotManagers[id]
    }

    fun getManuelBotManagers(): MutableMap<String, ManuelBotManager> {
        return manuelBotManagers
    }

    fun getFollowBotManagers(): MutableMap<String, FollowBotManager> {
        return followBotManagers
    }

    fun updateManuelBotManager(id: String, manuelBotManager: ManuelBotManager) {
        manuelBotManagers.remove(id)
        manuelBotDbHelper.removeBotById(id)
        manuelBotManagers[id] = manuelBotManager
        manuelBotDbHelper.insertBot(
            ManuelBotEntity(
                manuelBotManager.id,
                manuelBotManager.firstPairName,
                manuelBotManager.secondPairName,
                manuelBotManager.pairName,
                manuelBotManager.threshold,
                manuelBotManager.amount,
                manuelBotManager.exchangeMarket,
                manuelBotManager.status,
                manuelBotManager.apiKey,
                manuelBotManager.secretKey,
                manuelBotManager.openPosition
            )
        )
    }

    fun updateFollowBotManager(id: String, followBotManager: FollowBotManager) {
        followBotManagers.remove(id)
        followBotDbHelper.removeBotById(id)
        followBotManagers[id] = followBotManager
        followBotDbHelper.insertBot(
            FollowBotEntity(
                followBotManager.id,
                followBotManager.firstPairName,
                followBotManager.secondPairName,
                followBotManager.pairName,
                followBotManager.threshold,
                followBotManager.amount,
                followBotManager.exchangeMarket,
                followBotManager.status,
                followBotManager.apiKey,
                followBotManager.secretKey,
                followBotManager.openPosition,
                followBotManager.distanceInterval,
                followBotManager.followInterval
            )
        )
    }

    fun removeManuelBotManager(id: String) {
        val bot = manuelBotManagers[id]
        bot?.stop()
        manuelBotManagers.remove(id)
        manuelBotDbHelper.removeBotById(id)
    }

    fun removeFollowBotManager(id: String) {
        val bot = followBotManagers[id]
        bot?.stop()
        followBotManagers.remove(id)
        followBotDbHelper.removeBotById(id)
    }

    /**
     * This is the main resolved function now it doesn't create new object if this bot exist before
     * so now we can access same object reference and do operations that we want to do.
     */
    private fun loadManuelBotsFromDatabase() {
        val bots = manuelBotDbHelper.getAllBots()
        for (bot in bots) {
            if (manuelBotManagers.containsKey(bot.id)) {
                // BotManager zaten varsa güncelle YOKSA YENİ OBJE OLUŞTURULUYOR VE ONUN FONKSİYONLARI ÇAĞRILIYOR!
                val botManager = manuelBotManagers[bot.id]!!
                botManager.firstPairName = bot.firstPairName
                botManager.secondPairName = bot.secondPairName
                botManager.pairName = bot.pairName
                botManager.threshold = bot.threshold
                botManager.amount = bot.amount
                botManager.exchangeMarket = bot.exchangeMarket
                botManager.status = bot.status
                botManager.apiKey = bot.apiKey
                botManager.secretKey = bot.secretKey
                botManager.openPosition = bot.openPosition
                // BotManager güncellendiğinde diğer işlemleri yapabilirsiniz (örneğin, yeniden başlatma)
            } else {
                // Yeni bir BotManager oluştur
                val manuelBotManager = ManuelBotManager(
                    bot.id,
                    bot.firstPairName,
                    bot.secondPairName,
                    bot.pairName,
                    bot.threshold,
                    bot.amount,
                    bot.exchangeMarket,
                    bot.status,
                    bot.apiKey,
                    bot.secretKey,
                    bot.openPosition
                )
                manuelBotManagers[bot.id] = manuelBotManager
            }
        }
    }

    private fun loadFollowBotsFromDatabase() {
        val bots = followBotDbHelper.getAllBots()
        for (bot in bots) {
            if (followBotManagers.containsKey(bot.id)) {
                // BotManager zaten varsa güncelle YOKSA YENİ OBJE OLUŞTURULUYOR VE ONUN FONKSİYONLARI ÇAĞRILIYOR!
                val botManager = followBotManagers[bot.id]!!
                botManager.firstPairName = bot.firstPairName
                botManager.secondPairName = bot.secondPairName
                botManager.pairName = bot.pairName
                botManager.threshold = bot.threshold
                botManager.amount = bot.amount
                botManager.exchangeMarket = bot.exchangeMarket
                botManager.status = bot.status
                botManager.apiKey = bot.apiKey
                botManager.secretKey = bot.secretKey
                botManager.openPosition = bot.openPosition
                botManager.distanceInterval = bot.distanceInterval
                botManager.followInterval = bot.followInterval
                // BotManager güncellendiğinde diğer işlemleri yapabilirsiniz (örneğin, yeniden başlatma)
            } else {
                // Yeni bir BotManager oluştur
                val followBotManager = FollowBotManager(
                    bot.id,
                    bot.firstPairName,
                    bot.secondPairName,
                    bot.pairName,
                    bot.threshold,
                    bot.amount,
                    bot.exchangeMarket,
                    bot.status,
                    bot.apiKey,
                    bot.secretKey,
                    bot.openPosition,
                    bot.distanceInterval,
                    bot.followInterval
                )
                followBotManagers[bot.id] = followBotManager
            }
        }
    }
    fun removeAllManuelBots() {
        // Stop and remove all bots from memory
        for (botManager in manuelBotManagers.values) {
            botManager.stop()
        }
        manuelBotManagers.clear()

        // Remove all bots from the database
        manuelBotDbHelper.deleteAllBots()
    }
}


