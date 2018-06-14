package com.asfoundation.wallet.ui.iab;

public class Payment {
  private final Status status;
  private final String uri;
  private final String buyHash;

  public Payment(String uri, Status status, String buyHash) {
    this.status = status;
    this.uri = uri;
    this.buyHash = buyHash;
  }

  public Status getStatus() {
    return status;
  }

  public String getUri() {
    return uri;
  }

  public String getBuyHash() {
    return buyHash;
  }

  public enum Status {
    COMPLETED, NO_FUNDS, NETWORK_ERROR, NO_ETHER, NO_TOKENS, NO_INTERNET, NONCE_ERROR, APPROVING,
    BUYING, ERROR
  }
}

