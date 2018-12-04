package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created by trinkes on 3/15/18.
 */

public class PaymentTransaction {
  private final @Nullable String approveHash;
  private final @Nullable String buyHash;
  private final TransactionBuilder transactionBuilder;
  private final PaymentState state;
  private final String packageName;
  private final String productName;
  private final String productId;
  private final String developerPayload;
  private final String callbackUrl;

  public PaymentTransaction(TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, @Nullable String buyHash, String packageName,
      String productName, String productId, String developerPayload, String callbackUrl) {
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.approveHash = approveHash;
    this.buyHash = buyHash;
    this.packageName = packageName;
    this.productName = productName;
    this.productId = productId;
    this.developerPayload = developerPayload;
    this.callbackUrl = callbackUrl;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state) {
    this(paymentTransaction.getTransactionBuilder(), state,
        paymentTransaction.getApproveHash(), paymentTransaction.getBuyHash(),
        paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
        paymentTransaction.getProductId(), paymentTransaction.getDeveloperPayload(),
        paymentTransaction.getCallbackUrl());
  }

  public PaymentTransaction(TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, String packageName, String productName, String productId,
      String developerPayload, String callbackUrl) {
    this.approveHash = approveHash;
    this.packageName = packageName;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.productName = productName;
    this.productId = productId;
    this.buyHash = null;
    this.developerPayload = developerPayload;
    this.callbackUrl = callbackUrl;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash) {
    this(paymentTransaction.getTransactionBuilder(), state,
        approveHash, null, paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
        paymentTransaction.getProductId(), paymentTransaction.getDeveloperPayload(),
        paymentTransaction.getCallbackUrl());
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash, String buyHash) {
    this(paymentTransaction.getTransactionBuilder(), state,
        approveHash, buyHash, paymentTransaction.getPackageName(),
        paymentTransaction.getProductName(), paymentTransaction.getProductId(),
        paymentTransaction.getDeveloperPayload(), paymentTransaction.getCallbackUrl());
  }

  public PaymentTransaction(TransactionBuilder transactionBuilder, String packageName,
      String productName, String productId, String developerPayload, String callbackUrl) {
    this(transactionBuilder, PaymentState.PENDING, null, packageName, productName, productId,
        developerPayload, callbackUrl);
  }

  public String getPackageName() {
    return packageName;
  }

  public TransactionBuilder getTransactionBuilder() {
    return transactionBuilder;
  }

  public PaymentState getState() {
    return state;
  }

  @Nullable public String getBuyHash() {
    return buyHash;
  }

  public @Nullable String getApproveHash() {
    return approveHash;
  }

  @Override public int hashCode() {

    return Objects.hash(approveHash, buyHash, transactionBuilder, state, packageName, productName,
        productId, developerPayload, callbackUrl);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PaymentTransaction that = (PaymentTransaction) o;
    return Objects.equals(approveHash, that.approveHash)
        && Objects.equals(buyHash, that.buyHash)
        && Objects.equals(transactionBuilder, that.transactionBuilder)
        && state == that.state
        && Objects.equals(packageName, that.packageName)
        && Objects.equals(productName, that.productName)
        && Objects.equals(productId, that.productId)
        && Objects.equals(developerPayload, that.developerPayload)
        && Objects.equals(callbackUrl, that.callbackUrl);
  }

  @Override public String toString() {
    return "PaymentTransaction{" + "approveHash='"
        + approveHash
        + '\''
        + ", buyHash='"
        + buyHash
        + '\''
        + ", transactionBuilder="
        + transactionBuilder
        + ", state="
        + state
        + ", packageName='"
        + packageName
        + '\''
        + ", productName='"
        + productName
        + '\''
        + ", productId='"
        + productId
        + '\''
        + ", developerPayload='"
        + developerPayload
        + '\''
        + ", callbackUrl='"
        + callbackUrl
        + '\''
        + '}';
  }

  public String getProductName() {
    return productName;
  }

  public String getProductId() {
    return productId;
  }

  public String getDeveloperPayload() {
    return developerPayload;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public enum PaymentState {
    PENDING, APPROVING, APPROVED, BUYING, BOUGHT, COMPLETED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER, NO_FUNDS, NO_INTERNET
  }
}
