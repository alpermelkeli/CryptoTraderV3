package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.model.view.TradingBot
import com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.adapter.TradingBotViewHolder
import com.bumptech.glide.Glide

class TradingBotsAdapter(
    private val tradingBots: List<TradingBot>,
    private val clickListener: (TradingBot) -> Unit,
    private val longClickListener:(TradingBot) ->Unit
) : RecyclerView.Adapter<TradingBotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradingBotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trading_bots_item, parent, false)
        return TradingBotViewHolder(view,
            clickListener = {position -> clickListener(tradingBots[position])},
            longClickListener = {position->longClickListener(tradingBots[position])}
            )
        }
    override fun onBindViewHolder(holder: TradingBotViewHolder, position: Int) {
        val bot = tradingBots[position]
        holder.coinPairName.text = bot.pairName
        holder.exchangeMarketText.text = bot.exchangeMarket
        if(bot.status=="Passive") holder.activeText.setTextColor(Color.RED) else holder.activeText.setTextColor(Color.GREEN)
        holder.activeText.text = bot.status
        holder.botPositionText.text = bot.position
        if(bot.position=="Açık") holder.botPositionText.setTextColor(Color.GREEN) else holder.botPositionText.setTextColor(Color.RED)
        val coinImageUrl = getCoinImageUrl(bot.firstPairName)
        loadCoinImage(coinImageUrl, holder)
    }
    override fun getItemCount(): Int = tradingBots.size
    private fun getCoinImageUrl(firstPairName: String): String {
        return "https://assets.coincap.io/assets/icons/${firstPairName.lowercase()}@2x.png"
    }
    private fun loadCoinImage(url: String, holder: TradingBotViewHolder) {
        Glide.with(holder.itemView.context)
            .load(url)
            .placeholder(R.drawable.icon_without_background)
            .error(R.drawable.btc_vector)
            .into(holder.coinImage)
    }
}
