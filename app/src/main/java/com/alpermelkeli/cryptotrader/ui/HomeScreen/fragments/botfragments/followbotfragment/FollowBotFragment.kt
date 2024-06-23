package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.followbotfragment

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.model.view.TradingBot
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.adapter.TradingBotsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alpermelkeli.cryptotrader.databinding.FragmentFollowBotBinding
import com.alpermelkeli.cryptotrader.model.FollowBotManager
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.delay

class FollowBotFragment : Fragment() {
    private lateinit var binding: FragmentFollowBotBinding
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
        binding = FragmentFollowBotBinding.inflate(layoutInflater)
        setProgressBarAnimation()
        initializeAccountOperations()
        return binding.root
    }
    private fun setProgressBarAnimation(){
        val animator = ObjectAnimator.ofFloat(binding.followBotFragmentProgressBar, "rotation", 0f, 360f)
        animator.duration = 630
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        if (::binanceAccountOperations.isInitialized) {
            setupRecyclerView()
        }
    }
    private fun setupRecyclerView() {
        tradingBots = mutableListOf()

        for((id,botManager) in BotManagerStorage.getFollowBotManagers()){
            tradingBots.add(TradingBot(id, R.drawable.btc_vector, botManager.exchangeMarket, botManager.status, botManager.firstPairName, botManager.secondPairName, botManager.pairName,if(botManager.openPosition) "Sell" else "Buy"))
        }

        adapter = TradingBotsAdapter(tradingBots,
            clickListener = { tradingBot -> openBotDetailsActivity(tradingBot)},
            longClickListener = {tradingBot -> showRemoveDialog(tradingBot) })

        binding.followBotsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.followBotsRecyclerView.adapter = adapter
    }
    private fun showRemoveDialog(tradingBot: TradingBot) {

        val dialogView = layoutInflater.inflate(R.layout.dialog_remove_bot, null)
        val textView = dialogView.findViewById<TextView>(R.id.removeBotText)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmRemoveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelRemoveButton)
        val dialog = AlertDialog.Builder(context,R.style.TransparentBackgroundDialog)
            .setView(dialogView)
            .create()
        textView.setText("${tradingBot.id} silmek istediğinizden emin misiniz?")
        dialog.show()

        confirmButton.setOnClickListener{
            removeTradingBot(tradingBot)
            dialog.dismiss()
        }
        cancelButton.setOnClickListener{
            dialog.dismiss()
        }
    }
    private fun removeTradingBot(tradingBot: TradingBot) {
        val position = tradingBots.indexOf(tradingBot)
        if (position != -1) {
            tradingBots.removeAt(position)
            adapter.notifyItemRemoved(position)
            BotManagerStorage.removeFollowBotManager(tradingBot.id)
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
        val exchangeMarketEditText = dialogView.findViewById<EditText>(R.id.exchangeMarketEditText)
        val switchText = dialogView.findViewById<TextView>(R.id.switchText)
        val buySellSwitch = dialogView.findViewById<MaterialSwitch>(R.id.buySellSwitch)
        val positiveButton = dialogView.findViewById<Button>(R.id.addBotButton)
        val negativeButton = dialogView.findViewById<Button>(R.id.cancelAddBotButton)


        val dialog = AlertDialog.Builder(context,R.style.TransparentBackgroundDialog)
            .setView(dialogView)
            .create()

        dialog.show()

        buySellSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateSwitch(isChecked, buySellSwitch, switchText)
        }

        positiveButton.setOnClickListener {
            val firstPairName = firstPairNameEditText.text.toString().uppercase().trim()
            val secondPairName = secondPairNameEditText.text.toString().uppercase().trim()
            val pairName =  firstPairName + secondPairName
            val exchangeMarket = exchangeMarketEditText.text.toString().uppercase().trim()
            val threshold = 0.0
            val amount = 0.0

            if (pairName.isNotEmpty() && exchangeMarket.isNotEmpty()) {
                val id = generateBotId()

                val newBot = TradingBot(id, R.drawable.btc_vector, exchangeMarket, "Passive", firstPairName, secondPairName, pairName, if(buySellSwitch.isChecked) "Sell" else "Buy")

                tradingBots.add(newBot)

                adapter.notifyItemInserted(tradingBots.size - 1)

                val followBotManager = FollowBotManager(id, firstPairName, secondPairName, pairName, threshold, amount, exchangeMarket, "Passive", binanceAccountOperations.apI_KEY, binanceAccountOperations.apI_SECRET,buySellSwitch.isChecked,0.0,0.0)

                BotManagerStorage.addFollowBotManager(followBotManager)

                dialog.dismiss()
            }
        }
        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

    }
    private fun generateBotId(): String {
        return "FOLLOWBOT_" + System.currentTimeMillis().toString()
    }
    private fun updateSwitch(isChecked: Boolean, switch: MaterialSwitch, switchTextView: TextView) {
        val trackColor = if (isChecked) R.color.red else R.color.green
        if(isChecked) switchTextView.setText("SellBuy") else switchTextView.setText("BuySell")
        switch.trackTintList = ContextCompat.getColorStateList(requireContext(), trackColor)
    }
    private fun openBotDetailsActivity(tradingBot: TradingBot) {
        val intent = Intent(context, FollowBotDetailsActivity::class.java).apply {
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
        removeProgressBar()
    }
    private fun removeProgressBar(){
        CoroutineScope(Dispatchers.Main).launch {
            delay(620)
            binding.followBotFragmentProgressBar.visibility = View.GONE
            binding.followBotLayout.visibility = View.VISIBLE
        }
    }
}
