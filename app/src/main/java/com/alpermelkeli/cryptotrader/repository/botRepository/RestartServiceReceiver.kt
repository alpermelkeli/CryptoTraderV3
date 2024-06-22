package com.alpermelkeli.cryptotrader.repository.botRepository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.alpermelkeli.cryptotrader.RESTART_SERVICE") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, BotService::class.java))
            }
            else{
                context.startService(Intent(context, BotService::class.java))
            }
        }
    }
}
