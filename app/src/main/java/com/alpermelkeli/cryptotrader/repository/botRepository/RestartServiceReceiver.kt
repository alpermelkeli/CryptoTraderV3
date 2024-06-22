package com.alpermelkeli.cryptotrader.repository.botRepository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.alpermelkeli.cryptotrader.RESTART_SERVICE") {
            val serviceIntent = Intent(context, BotService::class.java)
            context.startService(serviceIntent)
        }
    }
}
