package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.homefragment

import com.alpermelkeli.cryptotrader.model.BotManager
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentHomeBinding
import com.alpermelkeli.cryptotrader.model.TradingBot
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.adapter.TradingBotsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.lifecycleScope
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    /**
     * It's just for the view.
     */
    private lateinit var tradingBots: MutableList<TradingBot>
    private lateinit var adapter: TradingBotsAdapter
    private lateinit var binanceAccountOperations: BinanceAccountOperations


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        initializeAccountOperations()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (::binanceAccountOperations.isInitialized) {
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        tradingBots = mutableListOf()

        for((id,botManager) in BotManagerStorage.getBotManagers()){
            tradingBots.add(TradingBot(id, R.drawable.btc_vector, botManager.exchangeMarket, botManager.status, botManager.firstPairName, botManager.secondPairName, botManager.pairName))
        }

        adapter = TradingBotsAdapter(tradingBots,
            clickListener = { tradingBot -> openBotDetailsActivity(tradingBot)},
            longClickListener = {tradingBot -> showRemoveDialog(tradingBot) })

        binding.manuelBotsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.manuelBotsRecyclerView.adapter = adapter
    }

    private fun showRemoveDialog(tradingBot: TradingBot) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Botu Sil")
            .setMessage("${tradingBot.pairName} botunu silmek istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { _, _ ->
                removeTradingBot(tradingBot)
            }
            .setNegativeButton("Hayır", null)
            .create()
        dialog.show()
    }

    private fun removeTradingBot(tradingBot: TradingBot) {
        val position = tradingBots.indexOf(tradingBot)
        if (position != -1) {
            tradingBots.removeAt(position)
            adapter.notifyItemRemoved(position)
            BotManagerStorage.removeBotManager(tradingBot.id)
        }
    }

    private fun setupButtonListeners() {
        binding.newTradeBotButton.setOnClickListener {
            showAddBotDialog()
        }
    }

    private fun showAddBotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_bot, null)
        val firstPairNameEditText = dialogView.findViewById<EditText>(R.id.firstPairEditText)
        val secondPairNameEditText = dialogView.findViewById<EditText>(R.id.secondPairEditText)
        val thresholdEditText = dialogView.findViewById<EditText>(R.id.thresholdEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val positiveButton = dialogView.findViewById<Button>(R.id.addBotButton)
        val negativeButton = dialogView.findViewById<Button>(R.id.cancelAddBotButton)

        val dialog = AlertDialog.Builder(context,R.style.AddBotButtonDialog)
            .setView(dialogView)
            .create()

        dialog.show()


        positiveButton.setOnClickListener {
            val firstPairName = firstPairNameEditText.text.toString().uppercase().trim()
            val secondPairName = secondPairNameEditText.text.toString().uppercase().trim()
            val pairName =  firstPairName + secondPairName
            val threshold = thresholdEditText.text.toString().toDoubleOrNull()
            val amount = amountEditText.text.toString().toDoubleOrNull()

            if (pairName.isNotEmpty() && threshold != null && amount != null) {
                val id = generateBotId()
                val newBot = TradingBot(id, R.drawable.btc_vector, "BINANCE", "Active", firstPairName, secondPairName, pairName)

                tradingBots.add(newBot)

                adapter.notifyItemInserted(tradingBots.size - 1)

                val botManager = BotManager(id, firstPairName, secondPairName, pairName, threshold, amount, "BINANCE", "Active", binanceAccountOperations.apI_KEY, binanceAccountOperations.apI_SECRET)

                BotManagerStorage.addBotManager(botManager)

                startTradingBot(id)
                dialog.dismiss()
            }
        }
        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun generateBotId(): String {
        return "BOT_" + System.currentTimeMillis().toString()
    }


    private fun startTradingBot(id: String) {
        BotService.startBot(id)
    }

    private fun updateAccountBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val balance = binanceAccountOperations.accountBalance
            withContext(Dispatchers.Main) {
                binding.accountBalanceUsdtText.text = "%.2f USDT".format(balance)
            }
        }
    }

    private fun openBotDetailsActivity(tradingBot: TradingBot) {
        val intent = Intent(context, BotDetailsActivity::class.java).apply {
            putExtra("id", tradingBot.id)
            putExtra("pairName", tradingBot.pairName)
            putExtra("API_KEY", binanceAccountOperations.apI_KEY)
            putExtra("SECRET_KEY", binanceAccountOperations.apI_SECRET)
            putExtra("firstPairName", tradingBot.firstPairName)
            putExtra("secondPairName", tradingBot.secondPairName)
        }
        startActivity(intent)
    }

    private fun initializeAccountOperations() {
        CoroutineScope(Dispatchers.IO).launch {
            ApiStorage.initialize(requireContext())
            val selectedAPI = ApiStorage.getSelectedApi()
            withContext(Dispatchers.Main) {
                val API_KEY = selectedAPI?.apiKey ?: ""
                val SECRET_KEY = selectedAPI?.secretKey ?: ""
                binanceAccountOperations = BinanceAccountOperations(API_KEY, SECRET_KEY)
                onAccountOperationsInitialized()
            }
        }
    }

    private fun onAccountOperationsInitialized() {
        BotManagerStorage.initialize(requireContext())
        setupRecyclerView()
        setupButtonListeners()
        updateAccountBalance()
    }
}
