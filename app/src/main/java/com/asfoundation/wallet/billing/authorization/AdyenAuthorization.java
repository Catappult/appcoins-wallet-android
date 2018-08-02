package com.asfoundation.wallet.billing.authorization;

public class AdyenAuthorization {

  private final String session;
  private final Status status;

  public AdyenAuthorization(String session, Status status) {
    this.session = session;
    this.status = status;
  }

  public String getSession() {
    return session;
  }

  public Boolean isProcessing() {
    return status == AdyenAuthorization.Status.PROCESSING;
  }

  public Boolean isFailed() {
    return status == AdyenAuthorization.Status.FAILED;
  }

  public Boolean isCompleted() {
    return status == AdyenAuthorization.Status.REDEEMED;
  }

  public Boolean isPendingAuthorization() {
    return status == AdyenAuthorization.Status.PENDING;
  }

  public enum Status {
    PENDING, REDEEMED, PROCESSING, FAILED, EXPIRED
  }
}
