package com.asfoundation.wallet.ui.iab;

import java.io.Serializable;

/**
 * Created by franciscocalado on 26/07/2018.
 */

public class FiatValue implements Serializable {
  private double amount;
  private String currency;

  public FiatValue(double amount, String currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public double getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }
}
