package com.asfoundation.wallet.recover

sealed class RecoverWalletResult

data class SuccessfulRecover(val address: String) : RecoverWalletResult()

sealed class FailedRecover : RecoverWalletResult() {
  data class GenericError(val detail: String) : FailedRecover()
  data class InvalidPassword(val keystore: String) : FailedRecover()
  data class RequirePassword(val address: String, val amount: String, val symbol: String) :
      FailedRecover()

  object AlreadyAdded : FailedRecover()
  object InvalidKeystore : FailedRecover()
  object InvalidPrivateKey : FailedRecover()
}
