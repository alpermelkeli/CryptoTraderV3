package com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance;

public interface KlineListener {
    void onKlineProcessed(String symbol, double percentChange, double enterPrice);
}
