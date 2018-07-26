package com.asfoundation.wallet.entity;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ConvertToFiatResponseBody {
  private double amount;
  private String currency;

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
