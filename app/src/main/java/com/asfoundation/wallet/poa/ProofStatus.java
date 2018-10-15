package com.asfoundation.wallet.poa;

public enum ProofStatus {
  PROCESSING, SUBMITTING, COMPLETED, NO_FUNDS, NO_INTERNET, GENERAL_ERROR, NO_WALLET, CANCELLED, NOT_AVAILABLE, NOT_AVAILABLE_ON_COUNTRY, ALREADY_REWARDED, INVALID_DATA;

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
      case NOT_AVAILABLE:
      case NOT_AVAILABLE_ON_COUNTRY:
      case ALREADY_REWARDED:
      case INVALID_DATA:
      default:
        return true;
    }
  }
}