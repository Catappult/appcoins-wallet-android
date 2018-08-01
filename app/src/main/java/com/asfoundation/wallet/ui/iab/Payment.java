package com.asfoundation.wallet.ui.iab;

import javax.annotation.Nullable;

public class Payment {
  private final Status status;
  private final String uri;
  private @Nullable final String fromAddress;
  private @Nullable final String buyHash;
  private @Nullable final String packageName;
  private @Nullable final String productName;

  public Payment(String uri, Status status, @Nullable String fromAddress, @Nullable String buyHash,
      @Nullable String packageName, @Nullable String productName) {
    this.status = status;
    this.uri = uri;
    this.fromAddress = fromAddress;
    this.buyHash = buyHash;
    this.packageName = packageName;
    this.productName = productName;
  }

  public Payment(String uri, Status status) {
    this.uri = uri;
    this.status = status;
    this.fromAddress = null;
    this.buyHash = null;
    this.packageName = null;
    this.productName = null;
  }

  @Nullable public String getFromAddress() {
    return fromAddress;
  }

  public Status getStatus() {
    return status;
  }

  public String getUri() {
    return uri;
  }

  public @Nullable String getBuyHash() {
    return buyHash;
  }

  public @Nullable String getPackageName() {
    return packageName;
  }

  public @Nullable String getProductName() {
    return productName;
  }

  public enum Status {
    COMPLETED, NO_FUNDS, NETWORK_ERROR, NO_ETHER, NO_TOKENS, NO_INTERNET, NONCE_ERROR, APPROVING,
    BUYING, ERROR
  }
}

