package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment.adapter.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.model.view.Coin

class WalletAdapter(private val coinList: List<Coin>) : RecyclerView.Adapter<WalletViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin_balance, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val coin = coinList[position]
        holder.coinName.text = coin.coinName
        holder.quantity.text = String.format("%.3f", coin.quantity)
        holder.worth.text = String.format("%.3f", coin.worth)
    }

    override fun getItemCount(): Int {
        return coinList.size
    }
}
