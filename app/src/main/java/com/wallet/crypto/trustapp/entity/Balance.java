package com.wallet.crypto.trustapp.entity;

public class Balance {
  private final String tokenSymbol;
  private final long tokenBalance;
  private final long fiatBalance;
  private final long fiatSymbol;

  public Balance(String tokenSymbol, long tokenBalance, long fiatBalance, long fiatSymbol) {
    this.tokenSymbol = tokenSymbol;
    this.tokenBalance = tokenBalance;
    this.fiatBalance = fiatBalance;

    this.fiatSymbol = fiatSymbol;
  }

  public long getFiatBalance() {
    return fiatBalance;
  }

  public long getFiatSymbol() {
    return fiatSymbol;
  }

  public String getTokenSymbol() {
    return tokenSymbol;
  }

  public long getTokenBalance() {
    return tokenBalance;
  }
}
