package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment.adapter.wallet

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alpermelkeli.cryptotrader.R

class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val coinName: TextView = itemView.findViewById(R.id.coin_name)
    val quantity: TextView = itemView.findViewById(R.id.coin_quantity)
    val worth: TextView = itemView.findViewById(R.id.coin_worth)
}