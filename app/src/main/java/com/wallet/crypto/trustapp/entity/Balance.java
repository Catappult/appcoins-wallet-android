package com.wallet.crypto.trustapp.entity;

public class Balance {
    private final String Symbol;
    private final long balance;

    public Balance(String symbol, long balance) {
        Symbol = symbol;
        this.balance = balance;

    }

    public String getSymbol() {
        return Symbol;
    }

    public long getBalance() {
        return balance;
    }
}
