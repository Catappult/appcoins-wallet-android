package com.asfoundation.wallet.poa

enum class ProofStatus {
  PROCESSING, SUBMITTING, COMPLETED, NO_FUNDS, NO_INTERNET, GENERAL_ERROR, NO_WALLET, CANCELLED,
  NOT_AVAILABLE, NOT_AVAILABLE_ON_COUNTRY, ALREADY_REWARDED, INVALID_DATA, PHONE_NOT_VERIFIED;

  val isTerminate: Boolean
    get() {
      return when (this) {
        PROCESSING,
        SUBMITTING,
        PHONE_NOT_VERIFIED -> false
        COMPLETED,
        NO_FUNDS,
        NO_INTERNET,
        GENERAL_ERROR,
        NO_WALLET,
        CANCELLED,
        NOT_AVAILABLE,
        NOT_AVAILABLE_ON_COUNTRY,
        ALREADY_REWARDED,
        INVALID_DATA -> true
      }
    }
}