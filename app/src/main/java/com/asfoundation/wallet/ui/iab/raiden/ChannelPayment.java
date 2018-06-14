package com.asfoundation.wallet.ui.iab.raiden;

import java.math.BigDecimal;
import javax.annotation.Nullable;

public class ChannelPayment {

  private final String id;
  private final Status status;
  private final String fromAddress;
  private final BigDecimal amount;
  private final String toAddress;
  private final @Nullable String hash;

  public ChannelPayment(String id, Status status, String fromAddress, BigDecimal amount,
      String toAddress) {
    this(id, status, fromAddress, amount, toAddress, null);
  }

  public ChannelPayment(Status status, ChannelPayment payment) {
    this(payment.getId(), status, payment.getFromAddress(), payment.getAmount(),
        payment.getToAddress(), null);
  }

  public ChannelPayment(String id, Status completed, String fromAddress, BigDecimal amount,
      String toAddress, String hash) {
    this.id = id;
    status = completed;
    this.fromAddress = fromAddress;
    this.amount = amount;
    this.toAddress = toAddress;
    this.hash = hash;
  }

  public String getId() {
    return id;
  }

  public Status getStatus() {
    return status;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public BigDecimal getAmount() {
    return amount;
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
