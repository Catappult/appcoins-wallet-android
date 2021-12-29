package com.asfoundation.wallet.recover

import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Single

sealed class RecoverWalletResult

data class SuccessfulWalletRecover(val address: String) : RecoverWalletResult()

sealed class FailedWalletRecover : RecoverWalletResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedWalletRecover()
  data class RequirePassword(val throwable: Throwable? = null, val address: String,
                             val amount: String,
                             val symbol: String) : FailedWalletRecover()

  data class InvalidPassword(val throwable: Throwable? = null) : FailedWalletRecover()
  data class AlreadyAdded(val throwable: Throwable? = null) : FailedWalletRecover()
  data class InvalidKeystore(val throwable: Throwable? = null) : FailedWalletRecover()
  data class InvalidPrivateKey(val throwable: Throwable? = null) : FailedWalletRecover()
}

class RecoverWalletResultMapper(private val getWalletInfoUseCase: GetWalletInfoUseCase,
                                private val currencyFormatUtils: CurrencyFormatUtils) {
  fun map(restoreResult: RestoreResult): Single<RecoverWalletResult> {
    return when (restoreResult) {
      is SuccessfulRestore ->
        Single.just(SuccessfulWalletRecover(restoreResult.address))
      is FailedRestore.AlreadyAdded ->
        Single.just(FailedWalletRecover.AlreadyAdded(restoreResult.throwable))
      is FailedRestore.GenericError ->
        Single.just(FailedWalletRecover.GenericError(restoreResult.throwable))
      is FailedRestore.InvalidKeystore ->
        Single.just(FailedWalletRecover.InvalidKeystore(restoreResult.throwable))
      is FailedRestore.InvalidPassword ->
        Single.just(FailedWalletRecover.InvalidPassword(restoreResult.throwable))
      is FailedRestore.InvalidPrivateKey ->
        Single.just(FailedWalletRecover.InvalidPrivateKey(restoreResult.throwable))
      is FailedRestore.RequirePassword -> {
        getWalletInfoUseCase(address = restoreResult.address, cached = false, updateFiat = true)
            .map {
              FailedWalletRecover.RequirePassword(restoreResult.throwable, it.wallet,
                  currencyFormatUtils.formatCurrency(
                      it.walletBalance.overallFiat.amount), it.walletBalance.overallFiat.symbol)
            }
      }
    }
  }
}
