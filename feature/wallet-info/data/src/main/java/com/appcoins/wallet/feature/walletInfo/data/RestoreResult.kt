package com.appcoins.wallet.feature.walletInfo.data

/**
 * Moved to this module temporarily, should be moved to the recover module when its available
 */
sealed class RestoreResult

data class SuccessfulRestore(val address: String) : RestoreResult()

sealed class FailedRestore : RestoreResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedRestore()
  data class InvalidPassword(val throwable: Throwable? = null, val address: String) :
    FailedRestore()

  data class AlreadyAdded(val throwable: Throwable? = null) : FailedRestore()
  data class InvalidKeystore(val throwable: Throwable? = null) : FailedRestore()
  data class InvalidPrivateKey(val throwable: Throwable? = null) : FailedRestore()
}

class RestoreResultErrorMapper {
  fun map(throwable: Throwable, address: String): RestoreResult {
    if (throwable.message != null) {
      if ((throwable.message as String).contains("Invalid Keystore", true)) {
        return FailedRestore.InvalidKeystore(throwable)
      }
      return when (throwable.message) {
        "Invalid password provided" -> FailedRestore.InvalidPassword(throwable, address)
        "Already added" -> FailedRestore.AlreadyAdded(throwable)
        else -> FailedRestore.GenericError(throwable)
      }
    } else {
      return FailedRestore.GenericError(throwable)
    }
  }
}
