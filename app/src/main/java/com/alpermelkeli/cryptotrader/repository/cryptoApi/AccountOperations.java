package com.alpermelkeli.cryptotrader.repository.cryptoApi;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.alpermelkeli.cryptotrader.model.view.Coin;
import com.alpermelkeli.cryptotrader.model.view.Trade;
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.TradeResult;

public interface AccountOperations {
    CompletableFuture<TradeResult> buyCoin(String symbol, double quantity);
    CompletableFuture<TradeResult> sellCoin(String symbol, double quantity);
    CompletableFuture<Double> getAccountBalance();
    CompletableFuture<List<Coin>> getAccountWallet();
    CompletableFuture<Double> getSelectedCoinQuantity(String asset);
    CompletableFuture<List<Trade>> getTradeHistorySelectedCoin(String pairName);
    CompletableFuture<TradeResult> sellCoinWithUSDT(String symbol, double usdtQuantity);
    CompletableFuture<TradeResult> buyCoinWithUSDT(String symbol, double usdtQuantity);
    CompletableFuture<Boolean> openLongToCoin(String symbol, double quantity, int leverage);
    CompletableFuture<Boolean> openShortToCoin(String symbol, double quantity, int leverage);
}
