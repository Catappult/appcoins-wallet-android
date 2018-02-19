package com.asf.wallet.token;

import com.asf.wallet.BuildConfig;

/**
 * Created by neuro on 07-02-2018.
 */

public enum Erc20Token {
  APPC(BuildConfig.DEFAULT_TOKEN_SYMBOL, BuildConfig.DEFAULT_TOKEN_DECIMALS,
      BuildConfig.DEFAULT_TOKEN_ADDRESS),;

  private final String address;
  private final String symbol;
  private final int decimals;

  Erc20Token(String symbol, int decimals, String address) {
    this.address = address;
    this.symbol = symbol;
    this.decimals = decimals;
  }

  public String getAddress() {
    return address;
  }

  public String getSymbol() {
    return symbol;
  }

  public int getDecimals() {
    return decimals;
  }
}
