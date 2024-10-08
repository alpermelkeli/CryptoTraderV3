package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.manuelbotfragment

import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpermelkeli.cryptotrader.databinding.ActivityManuelBotDetailsBinding
import com.alpermelkeli.cryptotrader.model.view.Trade
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.adapter.TradesAdapter
import com.alpermelkeli.cryptotrader.viewmodel.AccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class ManuelBotDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManuelBotDetailsBinding
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var adapter: TradesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)

        binding = ActivityManuelBotDetailsBinding.inflate(layoutInflater)

        val botManagerID = intent.getStringExtra("id")!!

        val pairName = intent.getStringExtra("pairName")!!

        val API_KEY = intent.getStringExtra("API_KEY")!!

        val SECRET_KEY = intent.getStringExtra("SECRET_KEY")!!


        accountViewModel = AccountViewModel(API_KEY,SECRET_KEY)

        accountViewModel.getTradeHistory(pairName)


        setContentView(binding.root)

        binding.tradeHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

        setUpWebView(pairName)

        setUpView(botManagerID!!)

        binding.backButton.setOnClickListener { finish() }

        binding.passiveButton.setOnClickListener { stopTradingBot(botManagerID) }

        binding.updateButton.setOnClickListener { updateTradingBot(botManagerID,binding.amountEditText.text.toString().toDouble(), binding.thresholdEditText.text.toString().toDouble()) }

    }

    private fun setUpView(botManagerID:String){
        val botManager = botManagerID.let { BotManagerStorage.getManuelBotManager(it) }
        botManager?.let {
            val id = botManager.id
            val firstPairName = botManager.firstPairName
            val secondPairName = botManager.secondPairName
            val pairName = botManager.pairName
            val amount = botManager.amount
            val threshold = botManager.threshold
            val openPosition = botManager.openPosition
            getPairsQuantities(firstPairName,secondPairName)
            setUpTradeHistoryRecycler()
            binding.botIdText.text = id
            binding.pairText.text = pairName
            binding.firstPairText.text = firstPairName
            binding.secondPairText.text = secondPairName
            binding.amountEditText.setText(amount.toString())
            binding.thresholdEditText.setText(threshold.toString())
            binding.openPositionText.text = if(openPosition) "Pozisyon Açık" else "Pozisyon Kapalı"
        }
    }
    private fun setUpTradeHistoryRecycler(){
        accountViewModel.tradeHistory.observe(this){
            adapter = TradesAdapter(it)
            binding.tradeHistoryRecyclerView.adapter = adapter
        }
    }
    private fun setUpWebView(pairName: String) {
        val webview = binding.tradingViewWebView
        webview.settings.javaScriptEnabled = true
        webview.webViewClient = WebViewClient()
        //Dynamic html content.
        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>TradingView Chart</title>
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                }
                .tradingview-widget-container {
                    height: 100%;
                    width: 100%;
                }
            </style>
        </head>
        <body>
        <!-- TradingView Widget BEGIN -->
        <div class="tradingview-widget-container" style="height:100%;width:100%">
            <div class="tradingview-widget-container__widget" style="height:calc(100% - 32px);width:100%"></div>
            <script type="text/javascript" src="https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js" async>
            {
              "autosize": true,
              "symbol": "BINANCE:$pairName",
              "interval": "240",
              "timezone": "Europe/Istanbul",
              "theme": "dark",
              "style": "1",
              "locale": "en",
              "backgroundColor": "rgba(0, 0, 0, 1)",
              "gridColor": "rgba(0, 0, 0, 0.06)",
              "hide_legend": true,
              "allow_symbol_change": true,
              "calendar": false,
              "support_host": "https://www.t    radingview.com"
            }
            </script>
        </div>
        <!-- TradingView Widget END -->
        </body>
        </html>
    """.trimIndent()

        webview.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    private fun getPairsQuantities(firstPair: String, secondPair: String) {
        accountViewModel.getFirstPairQuantity(firstPair)
        accountViewModel.getSecondPairQuantity(secondPair)
        accountViewModel.firstPairQuantity.observe(this){
            binding.firstPairQuantityText.text = it.toString()
        }
        accountViewModel.secondPairQuantity.observe(this){
            binding.secondPairQuantityText.text = it.toString()
        }
    }
    private fun stopTradingBot(id: String) {
        BotService.stopManuelBot(id)
    }
    private fun updateTradingBot(id:String,amount:Double,threshold:Double){
        BotService.updateManuelBot(id,amount,threshold)
    }

}
