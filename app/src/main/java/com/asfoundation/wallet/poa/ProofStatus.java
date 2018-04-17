package com.asfoundation.wallet.poa;

public enum ProofStatus {
  PROCESSING(false), SUBMITTING(false), COMPLETED(true), NO_FUNDS(true), NO_INTERNET(
      true), GENERAL_ERROR(true), NO_WALLET(true), CANCELLED(true);

  private final boolean isTerminate;

  ProofStatus(boolean isTerminate) {
    this.isTerminate = isTerminate;
  }

  public boolean isTerminate() {
    return isTerminate;
  }
}
