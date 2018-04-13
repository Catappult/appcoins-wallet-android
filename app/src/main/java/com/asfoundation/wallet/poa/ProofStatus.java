package com.asfoundation.wallet.poa;

public enum ProofStatus {
  PROCESSING(false), SUBMITTING(false), COMPLETED(false), NO_FUNDS(true), NO_INTERNET(
      true), GENERAL_ERROR(true), NO_WALLET(true);

  private final boolean isError;

  ProofStatus(boolean isError) {
    this.isError = isError;
  }

  public boolean isError() {
    return isError;
  }
}
