package com.asfoundation.wallet.ui.iab.raiden;

import java.math.BigDecimal;
import javax.annotation.Nullable;

public class ChannelPayment {

  private final String id;
  private final Status status;
  private final String fromAddress;
  private final BigDecimal ammount;
  private final String toAddress;
  private final BigDecimal channelBudget;
  private final @Nullable String hash;

  public ChannelPayment(String id, Status status, String fromAddress, BigDecimal ammount,
      String toAddress, BigDecimal channelBudget) {
    this(id, status, fromAddress, ammount, toAddress, channelBudget, null);
  }

  public ChannelPayment(Status status, ChannelPayment payment) {
    this(payment.getId(), status, payment.getFromAddress(), payment.getAmmount(),
        payment.getToAddress(), payment.getChannelBudget(), null);
  }

  public ChannelPayment(String id, Status completed, String fromAddress, BigDecimal ammount,
      String toAddress, BigDecimal channelBudget, String hash) {
    this.id = id;
    status = completed;
    this.fromAddress = fromAddress;
    this.ammount = ammount;
    this.toAddress = toAddress;
    this.channelBudget = channelBudget;
    this.hash = hash;
  }

  public String getId() {
    return id;
  }

  public BigDecimal getChannelBudget() {
    return channelBudget;
  }

  public Status getStatus() {
    return status;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public BigDecimal getAmmount() {
    return ammount;
  }

  public String getToAddress() {
    return toAddress;
  }

  public String getHash() {
    return hash;
  }

  public enum Status {
    PENDING, COMPLETED, ERROR, BUYING
  }
}
