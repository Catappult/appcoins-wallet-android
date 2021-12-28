package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.recover.FailedRecover
import com.asfoundation.wallet.recover.RecoverWalletResult

class RecoverErrorMapperUseCase {
  operator fun invoke(keystore: String, throwable: Throwable,
                      address: String, amount: String, symbol: String): RecoverWalletResult {
    if (throwable.message != null) {
      if ((throwable.message as String).contains("Invalid Keystore", true)) {
        return FailedRecover.InvalidKeystore
      }
      return when (throwable.message) {
        "Requires password" -> FailedRecover.RequirePassword(address, amount, symbol)
        "Invalid password provided" -> FailedRecover.InvalidPassword(keystore)
        "Already added" -> FailedRecover.AlreadyAdded
        else -> FailedRecover.GenericError(throwable.message!!)
      }
    } else {
      return FailedRecover.GenericError(throwable.message!!)
    }
  }
}