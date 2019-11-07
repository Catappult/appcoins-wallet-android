package com.asfoundation.wallet.poa

data class ProofSubmissionData(val status: RequirementsStatus, val hoursRemaining: Int = 0,
                               val minutesRemaining: Int = 0) {
  constructor(status: RequirementsStatus) : this(status, 0, 0)

  fun hasReachedPoaLimit() = hoursRemaining != 0 || minutesRemaining != 0

  enum class RequirementsStatus {
    READY, NO_FUNDS, NO_NETWORK, NO_WALLET, NOT_ELIGIBLE, WRONG_NETWORK, UNKNOWN_NETWORK,
    UPDATE_REQUIRED
  }
}
