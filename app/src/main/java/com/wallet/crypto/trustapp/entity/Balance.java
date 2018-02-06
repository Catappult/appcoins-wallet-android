package com.wallet.crypto.trustapp.entity;

public class Balance {
    private final String symbol;
    private final long balance;

    public Balance(String symbol, long balance) {
        this.symbol = symbol;
        this.balance = balance;

    }

    public String getSymbol() {
        return symbol;
    }

    public long getBalance() {
        return balance;
    }
}
