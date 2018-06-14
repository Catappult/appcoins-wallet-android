package com.asfoundation.wallet.ui.iab.raiden;

import java.math.BigDecimal;

public class ChannelCreation {
  private final Status status;
  private final String key;
  private final String address;
  private final BigDecimal budget;

  public ChannelCreation(String key, Status status, String address, BigDecimal budget) {
    this.status = status;
    this.key = key;
    this.address = address;
    this.budget = budget;
  }

  public ChannelCreation(ChannelCreation channelCreation, Status status) {
    this.key = channelCreation.getKey();
    this.status = status;
    this.address = channelCreation.getAddress();
    this.budget = channelCreation.getBudget();
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

  public enum Status {
    PENDING, CREATING, CREATED,ERROR
  }
}
