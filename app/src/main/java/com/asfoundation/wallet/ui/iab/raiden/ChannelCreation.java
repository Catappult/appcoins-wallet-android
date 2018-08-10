package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.repository.PaymentTransaction;
import java.math.BigDecimal;

public class ChannelCreation {
  private final Status status;
  private final String key;
  private final String address;
  private final BigDecimal budget;
  private final PaymentTransaction payment;

  public ChannelCreation(String key, Status status, String address, BigDecimal budget,
      PaymentTransaction payment) {
    this.status = status;
    this.key = key;
    this.address = address;
    this.budget = budget;
    this.payment = payment;
  }

  public ChannelCreation(ChannelCreation channelCreation, Status status) {
    this.key = channelCreation.getKey();
    this.status = status;
    this.address = channelCreation.getAddress();
    this.budget = channelCreation.getBudget();
    this.payment = channelCreation.getPayment();
  }

  public Status getStatus() {
    return status;
  }

  public String getKey() {
    return key;
  }

  public String getAddress() {
    return address;
  }

  public BigDecimal getBudget() {
    return budget;
  }

  public PaymentTransaction getPayment() {
    return payment;
  }

  public enum Status {
    PENDING, CREATING, CREATED, ERROR
  }
}
