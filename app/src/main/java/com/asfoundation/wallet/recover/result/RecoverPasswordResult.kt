package com.asfoundation.wallet.recover.result

import com.appcoins.wallet.feature.walletInfo.data.FailedRestore
import com.appcoins.wallet.feature.walletInfo.data.RestoreResult
import com.appcoins.wallet.feature.walletInfo.data.SuccessfulRestore
import com.asfoundation.wallet.entity.WalletKeyStore
import io.reactivex.Single

sealed class RecoverPasswordResult

data class SuccessfulPasswordRecover(val address: String, val name: String?) :
  RecoverPasswordResult()

sealed class FailedPasswordRecover : RecoverPasswordResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedPasswordRecover()
  data class InvalidPassword(val throwable: Throwable? = null) : FailedPasswordRecover()
}

class RecoverPasswordResultMapper(
  private val walletKeyStore: WalletKeyStore
) {
  fun map(restoreResult: RestoreResult): Single<RecoverPasswordResult> {
    return when (restoreResult) {
      is FailedRestore.GenericError ->
        Single.just(FailedPasswordRecover.GenericError(restoreResult.throwable))
      is FailedRestore.InvalidPassword ->
        Single.just(FailedPasswordRecover.InvalidPassword(restoreResult.throwable))
      is SuccessfulRestore ->
        Single.just(SuccessfulPasswordRecover(restoreResult.address, walletKeyStore.name))
      else -> TODO()
    }
  }
}
