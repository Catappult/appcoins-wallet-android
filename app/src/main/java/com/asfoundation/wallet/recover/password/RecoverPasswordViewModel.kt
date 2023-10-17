package com.asfoundation.wallet.recover.password

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.password.RecoverPasswordFragment.Companion.KEYSTORE_KEY
import com.asfoundation.wallet.recover.result.FailedPasswordRecover
import com.asfoundation.wallet.recover.result.RecoverPasswordResult
import com.asfoundation.wallet.recover.result.SuccessfulPasswordRecover
import com.asfoundation.wallet.recover.use_cases.RecoverPasswordKeystoreUseCase
import com.asfoundation.wallet.recover.use_cases.SetDefaultWalletUseCase
import com.asfoundation.wallet.recover.use_cases.UpdateBackupStateFromRecoverUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

sealed class RecoverPasswordSideEffect : SideEffect

data class RecoverPasswordState(
  val recoverResultAsync: Async<RecoverPasswordResult> = Async.Uninitialized
) :
  ViewState

@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
        private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
        private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
        private val walletsEventSender: WalletsEventSender,
        private val recoverPasswordKeystoreUseCase: RecoverPasswordKeystoreUseCase,
        private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
        private val updateBackupStateFromRecoverUseCase: UpdateBackupStateFromRecoverUseCase,
        private val updateWalletNameUseCase: UpdateWalletNameUseCase,
        private val savedStateHandle: SavedStateHandle,
) :
  BaseViewModel<RecoverPasswordState, RecoverPasswordSideEffect>(initialState()) {

  companion object {
    fun initialState(): RecoverPasswordState {
      return RecoverPasswordState()
    }
  }

  private fun setDefaultWallet(recoverResult: RecoverPasswordResult): Single<RecoverPasswordResult> {
    return when (recoverResult) {
      is FailedPasswordRecover -> Single.just(recoverResult)
      is SuccessfulPasswordRecover -> Completable.mergeArray(
        setDefaultWalletUseCase(recoverResult.address),
        updateWalletInfoUseCase(recoverResult.address)
      ).andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .andThen(Single.just(recoverResult))
    }
  }

  fun handleRecoverPasswordClick(password: String) {
    val keystore = savedStateHandle.get<WalletKeyStore>(KEYSTORE_KEY)
    recoverPasswordKeystoreUseCase(keyStore = keystore!!, password = password)
      .flatMap { setDefaultWallet(it) }
      .asAsyncToState {
        copy(recoverResultAsync = it)
      }
      .doOnSuccess { handleRecoverResult(it) }
      .doOnError {
        walletsEventSender.sendWalletCompleteRestoreEvent(
          WalletsAnalytics.STATUS_FAIL,
          it.message
        )
      }
      .scopedSubscribe()
  }

  private fun handleRecoverResult(recoverResult: RecoverPasswordResult) {
    when (recoverResult) {
      is SuccessfulPasswordRecover -> {
        updateWalletBackupState()
        walletsEventSender.sendWalletPasswordRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_SUCCESS
        )
      }
      is FailedPasswordRecover.InvalidPassword -> {
        walletsEventSender.sendWalletPasswordRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.throwable?.message
        )
      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.toString()
        )
      }
    }
  }

  private fun updateWalletBackupState() {
    updateBackupStateFromRecoverUseCase()
      .scopedSubscribe()
  }
}