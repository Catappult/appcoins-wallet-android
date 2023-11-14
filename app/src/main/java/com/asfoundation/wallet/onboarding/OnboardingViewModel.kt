package com.asfoundation.wallet.onboarding

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.RecoverEntryPrivateKeyUseCase
import com.asfoundation.wallet.recover.use_cases.SetDefaultWalletUseCase
import com.asfoundation.wallet.recover.use_cases.UpdateBackupStateFromRecoverUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : OnboardingSideEffect()
  object NavigateToWalletCreationAnimation : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToFinish : OnboardingSideEffect()
  object ShowLoadingRecover : OnboardingSideEffect()
}

data class OnboardingState(
  val pageContent: OnboardingContent = OnboardingContent.EMPTY,
  val walletCreationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val createWalletUseCase: CreateWalletUseCase,
  private val recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase,
  private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
  private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val updateBackupStateFromRecoverUseCase: UpdateBackupStateFromRecoverUseCase,
  private val walletsEventSender: WalletsEventSender,
  appStartUseCase: AppStartUseCase
) :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  init {
    handleLaunchMode(appStartUseCase)
  }

  private fun handleLaunchMode(appStartUseCase: AppStartUseCase) {
    viewModelScope.launch {
      when (appStartUseCase.startModes.first()) {
        is StartMode.PendingPurchaseFlow -> sendSideEffect { OnboardingSideEffect.NavigateToWalletCreationAnimation }
        is StartMode.GPInstall -> sendSideEffect { OnboardingSideEffect.NavigateToWalletCreationAnimation }
        else -> setState { copy(pageContent = OnboardingContent.VALUES) }
      }
    }
  }

  fun handleLaunchWalletClick() {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        setOnboardingCompletedUseCase()
        sendSideEffect {
          if (it) {
            OnboardingSideEffect.NavigateToFinish
          } else {
            OnboardingSideEffect.NavigateToWalletCreationAnimation
          }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { OnboardingSideEffect.NavigateToLink(uri) }
  }

  fun handleRecoverGuestWalletClick(backup: String?) {
    sendSideEffect { OnboardingSideEffect.ShowLoadingRecover }
    recoverEntryPrivateKeyUseCase(WalletKeyStore(null, backup ?: ""))
      .flatMap { setDefaultWallet(it) }
      .doOnSuccess { handleRecoverResult(it) }
      .doOnError {
        walletsEventSender.sendWalletCompleteRestoreEvent(
          WalletsAnalytics.STATUS_FAIL,
          it.message
        )
      }
      .scopedSubscribe()
  }

  private fun setDefaultWallet(recoverResult: RecoverEntryResult): Single<RecoverEntryResult> =
    when (recoverResult) {
      is FailedEntryRecover -> Single.just(recoverResult)
      is SuccessfulEntryRecover -> setDefaultWalletUseCase(recoverResult.address)
        .mergeWith(updateWalletInfoUseCase(recoverResult.address))
        .andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .toSingleDefault(recoverResult)
    }

  private fun handleRecoverResult(recoverResult: RecoverEntryResult) =
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
//        updateWalletBackupState()
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_SUCCESS
        )
        sendSideEffect { OnboardingSideEffect.NavigateToFinish }
      }
      is FailedEntryRecover.InvalidPassword -> {

      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.toString()
        )
      }
    }

  private fun updateWalletBackupState() {
    updateBackupStateFromRecoverUseCase().scopedSubscribe()
  }
}