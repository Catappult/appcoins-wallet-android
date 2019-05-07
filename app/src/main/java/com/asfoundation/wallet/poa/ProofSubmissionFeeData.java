package com.asfoundation.wallet.poa;

import java.math.BigDecimal;

public class ProofSubmissionFeeData {
  private final BigDecimal gasLimit;
  private final BigDecimal gasPrice;
  private final RequirementsStatus status;

  public ProofSubmissionFeeData(RequirementsStatus status, BigDecimal gasLimit,
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

  @Override public int hashCode() {
    int result = getGasLimit().hashCode();
    result = 31 * result + getGasPrice().hashCode();
    result = 31 * result + getStatus().hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProofSubmissionFeeData)) return false;

    ProofSubmissionFeeData that = (ProofSubmissionFeeData) o;

    if (!getGasLimit().equals(that.getGasLimit())) return false;
    if (!getGasPrice().equals(that.getGasPrice())) return false;
    return getStatus() == that.getStatus();
  }

  public enum RequirementsStatus {
    READY, NO_FUNDS, NO_NETWORK, NO_WALLET, WRONG_NETWORK, UNKNOWN_NETWORK
  }
}
