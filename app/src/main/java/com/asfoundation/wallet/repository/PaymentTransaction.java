package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import java.math.BigInteger;
import javax.annotation.Nullable;

/**
 * Created by trinkes on 3/15/18.
 */

public class PaymentTransaction {
  public static final BigInteger INVALID_NONCE = new BigInteger("-1");
  private final String uri;
  private final @Nullable String approveHash;
  private final @Nullable String buyHash;
  private final TransactionBuilder transactionBuilder;
  private final PaymentState state;
  private final BigInteger nonce;

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash, @Nullable String buyHash, BigInteger nonce) {
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    this.approveHash = approveHash;
    this.buyHash = buyHash;
    this.nonce = nonce;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        paymentTransaction.getApproveHash(), paymentTransaction.getBuyHash(),
        paymentTransaction.getNonce());
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state,
      @Nullable String approveHash) {
    this.approveHash = approveHash;
    this.uri = uri;
    this.transactionBuilder = transactionBuilder;
    this.state = state;
    buyHash = null;
    nonce = INVALID_NONCE;
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, null, paymentTransaction.getNonce());
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, PaymentState state,
      String approveHash, String buyHash) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(), state,
        approveHash, buyHash, paymentTransaction.getNonce());
  }

  public PaymentTransaction(String uri, TransactionBuilder transactionBuilder, PaymentState state) {
    this(uri, transactionBuilder, state, null);
  }

  public PaymentTransaction(PaymentTransaction paymentTransaction, BigInteger nonce) {
    this(paymentTransaction.getUri(), paymentTransaction.getTransactionBuilder(),
        paymentTransaction.getState(), paymentTransaction.getApproveHash(),
        paymentTransaction.getBuyHash(), nonce);
  }

  public BigInteger getNonce() {
    return nonce;
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
    PENDING, APPROVING, APPROVED, BUYING, BOUGHT, COMPLETED, ERROR, NO_FUNDS, WRONG_NETWORK,
    NONCE_ERROR, UNKNOWN_TOKEN, NO_INTERNET
  }
}
