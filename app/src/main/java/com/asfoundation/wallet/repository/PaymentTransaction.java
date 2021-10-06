package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created by trinkes on 3/15/18.
 */

public class PaymentTransaction {
  private final String uri;
  private final @Nullable String approveHash;
  private final @Nullable String buyHash;
  private final TransactionBuilder transactionBuilder;
  private final PaymentState state;
  private final String packageName;
  private final String productName;
  private final String productId;
  private final String developerPayload;
  private final String callbackUrl;
  private final String orderReference;
  private final Integer errorCode;
  private final String errorMessage;

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, @Nullable String buyHash, String packageName,
      String productName, String productId, String developerPayload, String callbackUrl,
      @Nullable String orderReference, @Nullable Integer errorCode, @Nullable String errorMessage) {
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.approveHash = approveHash;
    this.buyHash = buyHash;
    this.packageName = packageName;
    this.productName = productName;
    this.productId = productId;
    this.developerPayload = developerPayload;
    this.callbackUrl = callbackUrl;
    this.orderReference = orderReference;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        paymentTransaction.getApproveHash(), paymentTransaction.getBuyHash(),
        paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
        paymentTransaction.getProductId(), paymentTransaction.getDeveloperPayload(),
        paymentTransaction.getCallbackUrl(), paymentTransaction.getOrderReference(), null, null);
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      Integer errorCode, String errorMessage) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        paymentTransaction.getApproveHash(), paymentTransaction.getBuyHash(),
        paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
        paymentTransaction.getProductId(), paymentTransaction.getDeveloperPayload(),
        paymentTransaction.getCallbackUrl(), paymentTransaction.getOrderReference(), errorCode,
        errorMessage);
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, String packageName, String productName, String productId,
      String developerPayload, String callbackUrl, String orderReference) {
    this.approveHash = approveHash;
    this.packageName = packageName;
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.productName = productName;
    this.productId = productId;
    this.buyHash = null;
    this.developerPayload = developerPayload;
    this.callbackUrl = callbackUrl;
    this.orderReference = orderReference;
    this.errorCode = null;
    this.errorMessage = null;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, null, paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
        paymentTransaction.getProductId(), paymentTransaction.getDeveloperPayload(),
        paymentTransaction.getCallbackUrl(), paymentTransaction.getOrderReference(), null, null);
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash, String buyHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, buyHash, paymentTransaction.getPackageName(),
        paymentTransaction.getProductName(), paymentTransaction.getProductId(),
        paymentTransaction.getDeveloperPayload(), paymentTransaction.getCallbackUrl(),
        paymentTransaction.getOrderReference(), null, null);
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, String packageName,
      String productName, String productId, String developerPayload, String callbackUrl,
      String orderReference) {
    this(uri, transactionBuilder, PaymentState.PENDING, null, packageName, productName, productId,
        developerPayload, callbackUrl, orderReference);
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction,
      TransactionBuilder transactionBuilder) {
    this.uri = paymentTransaction.uri;
    this.approveHash = paymentTransaction.approveHash;
    this.buyHash = paymentTransaction.buyHash;
    this.transactionBuilder = transactionBuilder;
    this.state = paymentTransaction.state;
    this.packageName = paymentTransaction.packageName;
    this.productName = paymentTransaction.productName;
    this.productId = paymentTransaction.productId;
    this.developerPayload = paymentTransaction.developerPayload;
    this.callbackUrl = paymentTransaction.callbackUrl;
    this.orderReference = paymentTransaction.orderReference;
    this.errorCode = null;
    this.errorMessage = null;
  }

  public String getOrderReference() {
    return orderReference;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getUri() {
    return uri;
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
    int result = uri.hashCode();
    result = 31 * result + (approveHash != null ? approveHash.hashCode() : 0);
    result = 31 * result + (transactionBuilder != null ? transactionBuilder.hashCode() : 0);
    result = 31 * result + (state != null ? state.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PaymentTransaction)) return false;

    PaymentTransaction that = (PaymentTransaction) o;

    if (!uri.equals(that.uri)) return false;
    if (!Objects.equals(approveHash, that.approveHash)) {
      return false;
    }
    if (!Objects.equals(transactionBuilder, that.transactionBuilder)) {
      return false;
    }
    return state == that.state;
  }

  @Override public String toString() {
    return "PaymentTransaction{"
        + "uri='"
        + uri
        + '\''
        + ", approveHash='"
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

  public Integer getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public enum PaymentState {
    PENDING, APPROVING, APPROVED, BUYING, BOUGHT, COMPLETED, ERROR, WRONG_NETWORK, NONCE_ERROR,
    UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER, NO_FUNDS, NO_INTERNET, FORBIDDEN, SUB_ALREADY_OWNED
  }
}
