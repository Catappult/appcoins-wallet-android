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
  private final String packageName;
  private final String productName;
  private final String productId;

  public ChannelPayment(String id, Status status, String fromAddress, BigDecimal amount,
      String toAddress, String packageName, String productName, String productId) {
    this(id, status, fromAddress, amount, toAddress, null, packageName, productName, productId);
  }

  public ChannelPayment(Status status, ChannelPayment payment) {
    this(payment.getId(), status, payment.getFromAddress(), payment.getAmount(),
        payment.getToAddress(), null, payment.getPackageName(), payment.getProductName(), payment.productId);
  }

  public ChannelPayment(String id, Status completed, String fromAddress, BigDecimal amount,
      String toAddress, String hash, String packageName, String productName, String productId) {
    this.id = id;
    status = completed;
    this.fromAddress = fromAddress;
    this.amount = amount;
    this.toAddress = toAddress;
    this.hash = hash;
    this.packageName = packageName;
    this.productName = productName;
    this.productId = productId;
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

  public String getPackageName() {
    return packageName;
  }

  public String getProductName() {
    return productName;
  }

  public String getProductId() {
    return productId;
  }

  public enum Status {
    PENDING, COMPLETED, ERROR, BUYING
  }
}
