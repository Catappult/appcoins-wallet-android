package com.wallet.crypto.trustapp.token;

/**
 * Created by neuro on 07-02-2018.
 */

public enum Erc20Token {
  APPC("APPC", 18, "0x1a7a8bd9106f2b8d977e08582dc7d24c723ab0db");

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
