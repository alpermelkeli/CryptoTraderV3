package com.alpermelkeli.cryptotrader.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpermelkeli.cryptotrader.model.view.Coin
import com.alpermelkeli.cryptotrader.model.view.Trade
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

class AccountViewModel(apiKey:String,apiSecret:String) : ViewModel() {
    private val binanceAccountOperations = BinanceAccountOperations(apiKey,apiSecret)

    private val _balance : MutableLiveData<Double> = MutableLiveData()
    val balance : LiveData<Double> get() = _balance

    private val _accountWallet :MutableLiveData<List<Coin>> = MutableLiveData()
    val accountWallet : LiveData<List<Coin>> get() = _accountWallet

    private val _tradeHistory : MutableLiveData<List<Trade>> = MutableLiveData()
    val tradeHistory:LiveData<List<Trade>> get() = _tradeHistory

    private val _firstPairQuantity:MutableLiveData<Double> = MutableLiveData()
    val firstPairQuantity:LiveData<Double> get() = _firstPairQuantity

    private val _secondPairQuantity:MutableLiveData<Double> = MutableLiveData()
    val secondPairQuantity:LiveData<Double> get() = _secondPairQuantity

    fun getBalance(){
        viewModelScope.launch {
            binanceAccountOperations.accountBalance.await().let {
                _balance.postValue(it)
            }
        }
    }

    fun getAccountWallet(){
        viewModelScope.launch {
            binanceAccountOperations.accountWallet.await().let {
                it.sortByDescending { it.worth }
             _accountWallet.postValue(it)
            }
        }
    }

    fun getTradeHistory(pairName:String){
        viewModelScope.launch {
            binanceAccountOperations.getTradeHistorySelectedCoin(pairName).await().let {
                _tradeHistory.postValue(it)
            }
        }
    }

    fun getFirstPairQuantity(firstPair:String){
        viewModelScope.launch {
            binanceAccountOperations.getSelectedCoinQuantity(firstPair).await().let {
                _firstPairQuantity.postValue(it)
            }
        }
    }
    fun getSecondPairQuantity(secondPair:String){
        viewModelScope.launch {
            binanceAccountOperations.getSelectedCoinQuantity(secondPair).await().let {
                _secondPairQuantity.postValue(it)
            }
        }
    }



}