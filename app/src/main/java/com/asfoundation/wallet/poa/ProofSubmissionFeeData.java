package com.asfoundation.wallet.poa;

import java.math.BigDecimal;
import java.util.Objects;

public class ProofSubmissionFeeData {
  private final BigDecimal gasLimit;
  private final BigDecimal gasPrice;
  private final RequirementsStatus status;

  public  ProofSubmissionFeeData(RequirementsStatus status, BigDecimal gasLimit,
      BigDecimal gasPrice) {
    this.gasLimit = gasLimit;
    this.gasPrice = gasPrice;
    this.status = status;
  }

  public BigDecimal getGasLimit() {
    return gasLimit;
  }

  public BigDecimal getGasPrice() {
    return gasPrice;
  }

  public RequirementsStatus getStatus() {
    return status;
  }

  public enum RequirementsStatus {
    READY, NO_FUNDS, NO_NETWORK, NO_WALLET
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProofSubmissionFeeData)) return false;
    ProofSubmissionFeeData that = (ProofSubmissionFeeData) o;
    return Objects.equals(getGasLimit(), that.getGasLimit()) && Objects.equals(getGasPrice(),
        that.getGasPrice()) && getStatus() == that.getStatus();
  }

  @Override public int hashCode() {

    return Objects.hash(getGasLimit(), getGasPrice(), getStatus());
  }
}
