package com.asfoundation.wallet.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.asfoundation.wallet.home.HomeFragment
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.main.use_cases.GetCachedGuestWalletUseCase
import com.asfoundation.wallet.main.use_cases.HasAuthenticationPermissionUseCase
import com.asfoundation.wallet.main.use_cases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.onboarding.BackupModel
import com.asfoundation.wallet.onboarding.use_cases.GetResponseCodeWebSocketUseCase
import com.asfoundation.wallet.onboarding.use_cases.GetWsPortUseCase
import com.asfoundation.wallet.onboarding.use_cases.ShouldShowOnboardingUseCase
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.update_required.use_cases.GetAutoUpdateModelUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import com.asfoundation.wallet.verification.ui.paypal.VerificationPayPalProperties.PAYPAL_VERIFICATION_REQUIRED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainActivitySideEffect : SideEffect {
  object NavigateToOnboarding : MainActivitySideEffect()
  object NavigateToNavigationBar : MainActivitySideEffect()
  object NavigateToAutoUpdate : MainActivitySideEffect()
  object NavigateToFingerprintAuthentication : MainActivitySideEffect()
  object NavigateToPayPalVerification : MainActivitySideEffect()
  data class NavigateToGiftCard(val giftCard: String, val fromSplashScreen: Boolean) :
    MainActivitySideEffect()

  data class NavigateToPromoCode(val promoCode: String, val fromSplashScreen: Boolean) :
    MainActivitySideEffect()

  data class NavigateToOnboardingRecoverGuestWallet(val backupModel: BackupModel) :
    MainActivitySideEffect()
}

sealed interface SnackBarMessage {
  data class WalletSwitched(val walletName: String) : SnackBarMessage
  data object WalletAlreadyAdded : SnackBarMessage
  data object ShowNoMessage : SnackBarMessage
}

object MainActivityState : ViewState

@HiltViewModel
class MainActivityViewModel @Inject constructor(
  private val increaseLaunchCount: IncreaseLaunchCountUseCase,
  private val getAutoUpdateModelUseCase: GetAutoUpdateModelUseCase,
  private val hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase,
  private val hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
  private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
  private val getCachedGuestWalletUseCase: GetCachedGuestWalletUseCase,
  private val savedStateHandle: SavedStateHandle,
  private val rxSchedulers: RxSchedulers,
  private val getWsPortUseCase: GetWsPortUseCase,
  private val getResponseCodeWebSocketUseCase: GetResponseCodeWebSocketUseCase
) : BaseViewModel<MainActivityState, MainActivitySideEffect>(MainActivityState) {

  var isOnboardingPaymentFlow = false

  /**
   * Channel for snackBar messages.
   * The use flow's the [MainActivity] emitting messages and those messages being consumed by
   * the fragments instantiated by the [MainActivity].
   */
  private val _snackBarMessages = Channel<SnackBarMessage>(Channel.BUFFERED)

  /**
   * Flow for snackBar messages.
   */
  val snackBarMessages: Flow<SnackBarMessage> = _snackBarMessages.receiveAsFlow()

  init {
    handleSavedStateParameters()
  }

  fun handleInitialNavigation(
    authComplete: Boolean = false,
    giftCard: String? = null,
    promoCode: String? = null,
    fromSplashScreen: Boolean = false,
    newIntent: Boolean = false,
  ) {
    getAutoUpdateModelUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
        when {
          !newIntent && hasRequiredHardUpdateUseCase(blackList, updateVersionCode, updateMinSdk) ->
            sendSideEffect { MainActivitySideEffect.NavigateToAutoUpdate }

          !newIntent && hasAuthenticationPermissionUseCase() && !authComplete -> {
            sendSideEffect { MainActivitySideEffect.NavigateToFingerprintAuthentication }
          }

          !newIntent && shouldShowOnboardingUseCase() -> {
            getCachedGuestWalletUseCase()
              .subscribeOn(rxSchedulers.io)
              .observeOn(rxSchedulers.main)
              .doOnSuccess { backupModel ->
                if (backupModel != null && backupModel.backupPrivateKey.isNotEmpty()) {
                  sendSideEffect {
                    MainActivitySideEffect.NavigateToOnboardingRecoverGuestWallet(backupModel)
                  }
                  isOnboardingPaymentFlow = true
                } else
                  sendSideEffect { MainActivitySideEffect.NavigateToOnboarding }
              }
              .doOnError {
                sendSideEffect { MainActivitySideEffect.NavigateToOnboarding }
              }
              .scopedSubscribe()
          }

          giftCard != null ->
            sendSideEffect { MainActivitySideEffect.NavigateToGiftCard(giftCard, fromSplashScreen) }

          promoCode != null ->
            sendSideEffect {
              MainActivitySideEffect.NavigateToPromoCode(
                promoCode,
                fromSplashScreen
              )
            }

          else ->
            sendSideEffect { MainActivitySideEffect.NavigateToNavigationBar }
        }
      }
      .scopedSubscribe()
  }

  fun getWsPort(): String? {
    return getWsPortUseCase()
  }

  fun getResponseCodeWebSocket(): Int {
    return getResponseCodeWebSocketUseCase()
  }

  /**
   * Emits a [SnackBarMessage] to be consumed by the [HomeFragment].
   */
  fun emitSnackBarMessage(snackBarMessage: SnackBarMessage) {
    viewModelScope.launch {
      _snackBarMessages.send(snackBarMessage)
    }
  }

  private fun handleSavedStateParameters() {
    val fromSupportNotification = savedStateHandle.get<Boolean>(SUPPORT_NOTIFICATION_CLICK)
    val paypalVerificationRequired = savedStateHandle.get<Boolean>(PAYPAL_VERIFICATION_REQUIRED)
    if (fromSupportNotification == false) {
      // We only count a launch if it did not come from a notification
      increaseLaunchCount()
    }
    if (paypalVerificationRequired == true) {
      sendSideEffect { MainActivitySideEffect.NavigateToPayPalVerification }
    }
  }
}