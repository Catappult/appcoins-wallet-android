package com.asfoundation.wallet.recover.result

import io.reactivex.Single

sealed class RecoverPasswordResult

data class SuccessfulPasswordRecover(val address: String) : RecoverPasswordResult()

sealed class FailedPasswordRecover : RecoverPasswordResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedPasswordRecover()
  data class InvalidPassword(val throwable: Throwable? = null) : FailedPasswordRecover()
}

class RecoverPasswordResultMapper(keystore: String) {
  fun map(restoreResult: RestoreResult): Single<RecoverPasswordResult> {
    return when (restoreResult) {
      is FailedRestore.GenericError ->
        Single.just(FailedPasswordRecover.GenericError(restoreResult.throwable))
      is FailedRestore.InvalidPassword ->
        Single.just(FailedPasswordRecover.InvalidPassword(restoreResult.throwable))
      is SuccessfulRestore ->
        Single.just(SuccessfulPasswordRecover(restoreResult.address))
      else -> TODO()
    }
  }
}
