package com.asfoundation.wallet.poa;

public class ProofSubmissionFeeData {
  private final RequirementsStatus status;

  public ProofSubmissionFeeData(RequirementsStatus status) {
    this.status = status;
  }

  public RequirementsStatus getStatus() {
    return status;
  }

  @Override public int hashCode() {
    return getStatus().hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProofSubmissionFeeData)) return false;

    ProofSubmissionFeeData that = (ProofSubmissionFeeData) o;

    return getStatus() == that.getStatus();
  }

  public enum RequirementsStatus {
    READY, NO_FUNDS, NO_NETWORK, NO_WALLET, NOT_ELIGIBLE, NOT_AVAILABLE, WRONG_NETWORK,
    UNKNOWN_NETWORK
  }
}
