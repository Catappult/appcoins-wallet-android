package com.asfoundation.wallet.poa;

public enum ProofStatus {
  PROCESSING, SUBMITTING, COMPLETED, NO_FUNDS, NO_INTERNET, GENERAL_ERROR, NO_WALLET, CANCELLED;

  public boolean isTerminate() {
    switch (this) {
      case PROCESSING:
      case SUBMITTING:
        return false;
      case COMPLETED:
      case NO_FUNDS:
      case NO_INTERNET:
      case GENERAL_ERROR:
      case NO_WALLET:
      case CANCELLED:
      default:
        return true;
    }
  }
}
