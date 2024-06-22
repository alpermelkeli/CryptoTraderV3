package com.alpermelkeli.cryptotrader.repository.botRepository

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.model.FollowBotManager
import com.alpermelkeli.cryptotrader.model.ManuelBotManager
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen
import kotlinx.coroutines.*


/**
 * This class provides foreground service for the android app. It is manage bots by fetching data with using
 * BotManagerStorage(RAM) that has BotManager objects that has management features inside.
 */
class BotService : Service() {
    private lateinit var manuelBotManagers : MutableMap<String, ManuelBotManager>
    private lateinit var followBotManagers : MutableMap<String, FollowBotManager>
    override fun onCreate() {
        super.onCreate()
        BotManagerStorage.initialize(applicationContext)
        manuelBotManagers = BotManagerStorage.getManuelBotManagers()
        followBotManagers = BotManagerStorage.getFollowBotManagers()
        createNotificationChannel()
        instance = this
        Log.d("BotService", "Service created")
        Handler(Looper.getMainLooper()).postDelayed({
            restartAllBots()
        }, 5000)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, createNotification())
        }

        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent): IBinder? = null
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bot Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Channel for Bot Service" }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createNotification(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, HomeScreen::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CoinRobot")
            .setContentText("Servis Aktif!")
            .setSmallIcon(R.drawable.icon_without_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    private fun sendNotificationInternal(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.icon_without_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("BotService", "Service Destroyed")
        val restartServiceIntent = Intent(applicationContext, RestartServiceReceiver::class.java)
        restartServiceIntent.action = "com.alpermelkeli.cryptotrader.RESTART_SERVICE"
        val pendingIntent = PendingIntent.getBroadcast(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pendingIntent)
    }

    /*
    This function doesn't use because update function can control this.
     */
    private fun startManuelBot(id: String) {
        manuelBotManagers[id]?.let { botManager ->
            botManager.start()
            Log.d("BotService", "Bot $id started by WorkManager")
            manuelBotManagers[id] = botManager
        }
    }

    private fun startFollowBot(id: String) {
        followBotManagers[id]?.let { botManager ->
            botManager.start()
            Log.d("BotService", "Bot $id started by WorkManager")
            followBotManagers[id] = botManager
        }
    }
    private fun updateManuelBot(id: String, amount: Double, threshold: Double) {
        val botManager = manuelBotManagers[id]!!
        if(botManager.status=="Active"){
            botManager.stop()
            botManager.update(amount, threshold)
            botManager.start()
            Toast.makeText(application, "Bot updated.", Toast.LENGTH_LONG).show()
            Log.d("BotService", "Bot $id updated")
        }
        else{
            botManager.amount = amount
            botManager.threshold = threshold
            botManager.start()
            botManager.status = "Active"
            Toast.makeText(application, "Bot initialized and resumed.", Toast.LENGTH_LONG).show()
        }
        BotManagerStorage.updateManuelBotManager(id, botManager)
        manuelBotManagers[id] = botManager
    }
    private fun updateFollowBot(id: String, amount: Double, threshold: Double, distanceInterval:Double, followInterval:Double){
        val botManager = followBotManagers[id]!!
        if(botManager.status=="Active"){
            botManager.stop()
            botManager.update(amount, threshold, distanceInterval, followInterval)
            botManager.start()
            Toast.makeText(application, "Bot updated.", Toast.LENGTH_LONG).show()
            Log.d("BotService", "Bot $id updated")
        }
        else{
            botManager.amount = amount
            botManager.threshold = threshold
            botManager.distanceInterval = distanceInterval
            botManager.followInterval = followInterval
            botManager.start()
            botManager.status = "Active"
            Toast.makeText(application, "Bot initialized and resumed.", Toast.LENGTH_LONG).show()
        }
        BotManagerStorage.updateFollowBotManager(id, botManager)
        followBotManagers[id] = botManager
    }
    private fun stopManuelBot(id: String) {
        val botManager = manuelBotManagers[id]!!
        botManager.stop()
        botManager.status = "Passive"
        BotManagerStorage.updateManuelBotManager(id, botManager)
        Toast.makeText(applicationContext, "Bot stopped", Toast.LENGTH_LONG).show()
        Log.d("BotService", "Bot $id stopped")
        manuelBotManagers[id] = botManager
    }
    private fun stopFollowBot(id: String){
        val botManager = followBotManagers[id]!!
        botManager.stop()
        botManager.status = "Passive"
        BotManagerStorage.updateFollowBotManager(id, botManager)
        Toast.makeText(applicationContext, "Bot stopped", Toast.LENGTH_LONG).show()
        Log.d("BotService", "Bot $id stopped")
        followBotManagers[id] = botManager
    }
    private fun stopAllManuelBots() {
        CoroutineScope(Dispatchers.IO).launch {
            // Copy the botManagers to avoid ConcurrentModificationException
            val botManagersCopy = manuelBotManagers.toMap()

            for ((id, botManager) in botManagersCopy) {
                botManager.stop()
                botManager.status = "Passive"
                BotManagerStorage.updateManuelBotManager(id, botManager)
            }

            withContext(Dispatchers.Main) {
                Log.d("BotService", "All bots stopped")
                manuelBotManagers = BotManagerStorage.getManuelBotManagers()
                stopSelf()
            }
        }
    }
    private fun stopAllFollowBots() {
        CoroutineScope(Dispatchers.IO).launch {
            // Copy the botManagers to avoid ConcurrentModificationException
            val botManagersCopy = followBotManagers.toMap()

            for ((id, botManager) in botManagersCopy) {
                botManager.stop()
                botManager.status = "Passive"
                BotManagerStorage.updateFollowBotManager(id, botManager)
            }

            withContext(Dispatchers.Main) {
                Log.d("BotService", "All bots stopped")
                followBotManagers = BotManagerStorage.getFollowBotManagers()
                stopSelf()
            }
        }
    }
    /**
     * It's like bot viewmodel you can use this functions without create instance of this class.
     */
    companion object {
        private const val CHANNEL_ID = "BotServiceChannel"
        private lateinit var instance: BotService

        fun sendNotification(title: String, message: String) {
            instance.sendNotificationInternal(title, message)
        }
        fun stopService() {
            instance.stopAllManuelBots()
            instance.stopAllFollowBots()
            instance.stopSelf()
        }
        fun restartAllBots() {
            for((id,Bot) in instance.manuelBotManagers){
                Log.d("BotService", Bot.status)
                if(Bot.status=="Active") instance.startManuelBot(id)
            }
            for((id,Bot) in instance.followBotManagers){
                if(Bot.status=="Active") instance.startFollowBot(id)
            }
            Log.d("BotService", "All bots restarted")
        }
        fun startManuelBot(botId: String){
            instance.startManuelBot(botId)
        }
        fun startFollowBot(botId: String){
            instance.startFollowBot(botId)
        }
        fun updateManuelBot(botId: String, amount: Double, threshold: Double){
            instance.updateManuelBot(botId,amount,threshold)
        }
        fun updateFollowBot(botId: String,amount: Double,threshold: Double,distanceInterval: Double,followInterval: Double){
            instance.updateFollowBot(botId,amount,threshold,distanceInterval, followInterval)
        }
        fun stopManuelBot(botId: String){
            instance.stopManuelBot(botId)
        }
        fun stopFollowBot(botId: String){
            instance.stopFollowBot(botId)
        }

    }



}