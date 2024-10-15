package com.asfoundation.wallet.onboarding_new_payment.payment_methods

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCachedShowRefundDisclaimerUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetResponseCodeWebSocketUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_USER_CANCEL
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetCachedTransactionUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetFirstPaymentMethodsUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOtherPaymentMethodsUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingPaymentMethodsSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : OnboardingPaymentMethodsSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingPaymentMethodsSideEffect()

  data class showOrHideRefundDisclaimer(val showOrHideRefundDisclaimer: Boolean) :
    OnboardingPaymentMethodsSideEffect()
}

data class OnboardingPaymentMethodsState(
  val paymentMethodsAsync: Async<List<PaymentMethod>> = Async.Uninitialized,
  val otherPaymentMethods: List<PaymentMethod> = listOf(),
) : ViewState

@HiltViewModel
class OnboardingPaymentMethodsViewModel @Inject constructor(
  private val getFirstPaymentMethodsUseCase: GetFirstPaymentMethodsUseCase,
  private val getOtherPaymentMethodsUseCase: GetOtherPaymentMethodsUseCase, //temporary, to remove later and use getFirstPaymentMethodsUseCase only
  private val getCachedTransactionUseCase: GetCachedTransactionUseCase,
  private val events: OnboardingPaymentEvents,
  private val setResponseCodeWebSocketUseCase: SetResponseCodeWebSocketUseCase,
  savedStateHandle: SavedStateHandle,
  private val getCachedShowRefundDisclaimerUseCase: GetCachedShowRefundDisclaimerUseCase
) :
  BaseViewModel<OnboardingPaymentMethodsState, OnboardingPaymentMethodsSideEffect>(
    OnboardingPaymentMethodsState()
  ) {

  private var args: OnboardingPaymentMethodsFragmentArgs =
    OnboardingPaymentMethodsFragmentArgs.fromSavedStateHandle(savedStateHandle)

  init {
    handlePaymentMethods()
    getCachedRefundDisclaimerValue()
  }

  //TODO add events to payment start

  private fun handlePaymentMethods() {
    getCachedTransactionUseCase(currencyCode = args.currency, amount = args.amount.toDouble()).flatMap {
      if (it.type != args.transactionBuilder.type) {
        args.transactionBuilder.type = it.type
        args.transactionBuilder.wspPort = it.wsPort
        args.transactionBuilder.sdkVersion = it.sdkVersion
        args.transactionBuilder.origin = it.origin
        args.transactionBuilder.referrerUrl = it.referrerUrl
      }
      getFirstPaymentMethodsUseCase(cachedTransaction = it)
        // to only use getFirstPaymentMethodsUseCase and remove the doOnSuccess after all methods are ready
        .doOnSuccess { availablePaymentMethods ->
          setState {
            copy(otherPaymentMethods = getOtherPaymentMethodsUseCase(availablePaymentMethods))
          }
        }
        .asAsyncToState {
          copy(paymentMethodsAsync = it)
        }
    }.repeatableScopedSubscribe(OnboardingPaymentMethodsState::paymentMethodsAsync.name)
  }

  private fun getCachedRefundDisclaimerValue() {
    sendSideEffect {
      OnboardingPaymentMethodsSideEffect.showOrHideRefundDisclaimer(
        getCachedShowRefundDisclaimerUseCase()
      )
    }
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { OnboardingPaymentMethodsSideEffect.NavigateToLink(uri) }
  }

  fun handleBackToGameClick() {
    events.sendPaymentMethodEvent(args.transactionBuilder, null, "back_to_the_game")
    sendSideEffect { OnboardingPaymentMethodsSideEffect.NavigateBackToGame(args.transactionBuilder.domain) }
  }

  fun setDefaultResponseCodeWebSocket() {
    setResponseCodeWebSocketUseCase(SDK_STATUS_USER_CANCEL)
  }
}