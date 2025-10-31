package com.asfoundation.wallet.ui.login.webview_login.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.RecoverEntryPrivateKeyUseCase
import com.asfoundation.wallet.recover.use_cases.SetDefaultWalletUseCase
import com.asfoundation.wallet.ui.login.webview_login.repository.LoginRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class FetchUserKeyUseCase @Inject constructor(
  val loginRepository: LoginRepository,
  val recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase,
  val setDefaultWalletUseCase: SetDefaultWalletUseCase,
  val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  val updateWalletNameUseCase: UpdateWalletNameUseCase,
  val setActiveWalletUseCase: SetActiveWalletUseCase
) {

  operator fun invoke(
    authToken: String,
    email: String?
  ): Completable {
    return loginRepository.fetchUserKey(authToken)
      .flatMap { key ->
        recoverEntryPrivateKeyUseCase(keyStore = WalletKeyStore(email, key.userKey))
      }
      .flatMap { recoverResult ->
        setDefaultWallet(recoverResult, email)
      }
      .flatMapCompletable {
        Completable.complete()
      }
  }

  private fun setDefaultWallet(
    recoverResult: RecoverEntryResult,
    email: String?
  ): Single<RecoverEntryResult> =
    when (recoverResult) {
      /**
       * is FailedEntryRecover.AlreadyAdded -> {
       *         TODO
       *           """
       *             Handle case when wallet is already added:
       *               1. User makes log-in in a wallet that is already saved locally and is the current active wallet → “Already logged-in”
       *                   A. Same as above, but wallet is not active → “Switched account”
       *           """.trimIndent()
       *       }
       */

      is FailedEntryRecover -> Single.error(Exception("Failed to recover wallet: $recoverResult"))
      /**
       * TODO:
       *    User’s current active wallet has log-in information, makes login with a new wallet → “Switched account”
       *    The user is logging-in with a new email:
       *      active wallet does not have log-in information, wallet returned by log-in flow is the same address (wallet was saved to cloud, first login) → No message, normal successful web message.
       */
      is SuccessfulEntryRecover -> setDefaultWalletUseCase(recoverResult.address)
        .mergeWith(
          email?.let { updateWalletInfoUseCase(recoverResult.address, it) }
            ?: updateWalletInfoUseCase(recoverResult.address)
        )
        .andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .andThen(setActiveWalletUseCase(recoverResult.address))
        .toSingleDefault(recoverResult)
    }

}
