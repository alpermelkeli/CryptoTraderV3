package com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance;

public class TradeResult {
    private boolean success;
    private double commission;

    public TradeResult(boolean success, double commission) {
        this.success = success;
        this.commission = commission;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }
}
