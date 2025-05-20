package com.asfoundation.wallet.ui.webview_login.usecases

import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.RecoverEntryPrivateKeyUseCase
import com.asfoundation.wallet.recover.use_cases.SetDefaultWalletUseCase
import com.asfoundation.wallet.ui.webview_login.repository.LoginRepository
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
) {

  operator fun invoke(
    authToken: String,
  ): Completable{
    return loginRepository.fetchUserKey(authToken)
      .flatMap { key ->
        recoverEntryPrivateKeyUseCase(keyStore = WalletKeyStore(null, key))
      }
      .flatMapCompletable { recoverResult ->
        setDefaultWallet(recoverResult)
        Completable.complete()
      }
  }

  private fun setDefaultWallet(recoverResult: RecoverEntryResult): Single<RecoverEntryResult> =
    when (recoverResult) {
      is FailedEntryRecover -> Single.error(Exception("Failed to recover wallet: $recoverResult"))
      is SuccessfulEntryRecover -> setDefaultWalletUseCase(recoverResult.address)
        .mergeWith(updateWalletInfoUseCase(recoverResult.address))
        .andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .toSingleDefault(recoverResult)
    }

}
