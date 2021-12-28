package com.asfoundation.wallet.recover

import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase

sealed class RecoverWalletResult

data class SuccessfulWalletRecover(val address: String) : RecoverWalletResult()

sealed class FailedWalletRecover : RecoverWalletResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedWalletRecover()
  data class RequirePassword(val throwable: Throwable? = null, val address: String, val amount: String,
                             val symbol: String) : FailedWalletRecover()

  data class InvalidPassword(val throwable: Throwable? = null) : FailedWalletRecover()
  data class AlreadyAdded(val throwable: Throwable? = null) : FailedWalletRecover()
  data class InvalidKeystore(val throwable: Throwable? = null) : FailedWalletRecover()
  data class InvalidPrivateKey(val throwable: Throwable? = null) : FailedWalletRecover()
}

class RecoverWalletResultMapper(private val getWalletInfoUseCase: GetWalletInfoUseCase,
                                private val currencyFormatUtils: CurrencyFormatUtils) {
  fun map(restoreResult: RestoreResult): RecoverWalletResult {
    return when (restoreResult) {
      is SuccessfulRestore ->
        SuccessfulWalletRecover(restoreResult.address)
      is FailedRestore.AlreadyAdded ->
        FailedWalletRecover.AlreadyAdded(restoreResult.throwable)
      is FailedRestore.GenericError ->
        FailedWalletRecover.GenericError(restoreResult.throwable)
      is FailedRestore.InvalidKeystore ->
        FailedWalletRecover.InvalidKeystore(restoreResult.throwable)
      is FailedRestore.InvalidPassword ->
        FailedWalletRecover.InvalidPassword(restoreResult.throwable)
      is FailedRestore.InvalidPrivateKey ->
        FailedWalletRecover.InvalidPrivateKey(restoreResult.throwable)
      is FailedRestore.RequirePassword -> {
        getWalletInfoUseCase(address = restoreResult.address, cached = false, updateFiat = true)
            .map {
              FailedWalletRecover.RequirePassword(restoreResult.throwable, it.wallet,
                  currencyFormatUtils.formatCurrency(
                      it.walletBalance.overallFiat.amount), it.walletBalance.overallFiat.symbol)
            }
            .blockingGet()
      }
    }
  }
}
