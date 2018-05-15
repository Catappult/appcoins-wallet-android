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

  public enum RequirementsStatus {
    READY, NO_FUNDS, NO_WALLET
  }
}
