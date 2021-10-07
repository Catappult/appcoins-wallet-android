package com.asfoundation.wallet.ui.iab;

import javax.annotation.Nullable;

public class Payment {
  private final Status status;
  private final String uri;
  private @Nullable final String fromAddress;
  private @Nullable final String buyHash;
  private @Nullable final String packageName;
  private @Nullable final String productName;
  private @Nullable final String uid;
  private @Nullable final String purchaseUid;
  private @Nullable final String signature;
  private @Nullable final String signatureData;
  private @Nullable final String productId;
  private @Nullable final String orderReference;
  private @Nullable final Integer errorCode;
  private @Nullable final String errorMessage;

  public Payment(String uri, Status status, @Nullable String uid, @Nullable String purchaseUid,
      @Nullable String signature, @Nullable String signatureData, @Nullable String orderReference,
      @Nullable Integer errorCode, @Nullable String errorMessage) {
    this.status = status;
    this.uri = uri;
    this.fromAddress = null;
    this.buyHash = null;
    this.packageName = null;
    this.productName = null;
    this.uid = uid;
    this.purchaseUid = purchaseUid;
    this.signature = signature;
    this.signatureData = signatureData;
    this.productId = null;
    this.orderReference = orderReference;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public Payment(String uri, Status status, @Nullable String fromAddress, @Nullable String buyHash,
      @Nullable String packageName, @Nullable String productName, @Nullable String productId,
      @Nullable String orderReference, @Nullable Integer errorCode, @Nullable String errorMessage) {
    this.status = status;
    this.uri = uri;
    this.fromAddress = fromAddress;
    this.buyHash = buyHash;
    this.packageName = packageName;
    this.productName = productName;
    this.uid = null;
    this.purchaseUid = null;
    this.signature = null;
    this.signatureData = null;
    this.productId = productId;
    this.orderReference = orderReference;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  @Nullable public String getOrderReference() {
    return orderReference;
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

  public @Nullable String getProductId() {
    return productId;
  }

  @Nullable public String getSignatureData() {
    return signatureData;
  }

  @Nullable public String getUid() {
    return uid;
  }

  @Nullable public String getSignature() {
    return signature;
  }

  @Nullable public Integer getErrorCode() {
    return errorCode;
  }

  @Nullable public String getErrorMessage() {
    return errorMessage;
  }

  @Nullable public String getPurchaseUid() {
    return purchaseUid;
  }

  @Override public String toString() {
    return "Payment{"
        + "status="
        + status
        + ", uri='"
        + uri
        + '\''
        + ", fromAddress='"
        + fromAddress
        + '\''
        + ", buyHash='"
        + buyHash
        + '\''
        + ", packageName='"
        + packageName
        + '\''
        + ", productName='"
        + productName
        + '\''
        + ", uid='"
        + uid
        + '\''
        + ", purchaseUid='"
        + purchaseUid
        + '\''
        + ", signature='"
        + signature
        + '\''
        + ", signatureData='"
        + signatureData
        + '\''
        + ", productId='"
        + productId
        + '\''
        + '}';
  }

  public enum Status {
    COMPLETED, NO_FUNDS, NETWORK_ERROR, NO_ETHER, NO_TOKENS, NO_INTERNET, NONCE_ERROR, APPROVING,
    BUYING, FORBIDDEN, SUB_ALREADY_OWNED, ERROR
  }
}

