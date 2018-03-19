package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
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

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, @Nullable String buyHash) {
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.approveHash = approveHash;
    this.buyHash = buyHash;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        paymentTransaction.getApproveHash(), paymentTransaction.getBuyHash());
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash) {
    this.approveHash = approveHash;
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    buyHash = null;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, null);
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash, String buyHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, buyHash);
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state) {
    this(uri, transactionBuilder, state, null);
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
    if (approveHash != null ? !approveHash.equals(that.approveHash) : that.approveHash != null) {
      return false;
    }
    if (transactionBuilder != null ? !transactionBuilder.equals(that.transactionBuilder)
        : that.transactionBuilder != null) {
      return false;
    }
    return state == that.state;
  }

  @Override public String toString() {
    return "PaymentTransaction{"
        + "approveHash='"
        + approveHash
        + '\''
        + ", buyHash='"
        + buyHash
        + '\''
        + ", state="
        + state
        + ", transactionBuilder="
        + transactionBuilder
        + ", uri='"
        + uri
        + '\''
        + '}';
  }

  public enum PaymentState {
    PENDING, APPROVING, APPROVED, BUYING, BOUGHT, COMPLETED, ERROR
  }
}
