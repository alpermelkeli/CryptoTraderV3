package com.alpermelkeli.cryptotrader.repository.botRepository

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.alpermelkeli.cryptotrader.RESTART_SERVICE") {
            startBotServiceIfNotRunning(context)
        }
    }

    private fun startBotServiceIfNotRunning(context: Context) {
        if (!isServiceRunning(context, BotService::class.java)) {
            val intent = Intent(context, BotService::class.java).apply {
                action = "START_SERVICE"
            }
            context.startService(intent)
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
