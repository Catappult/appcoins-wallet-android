package com.asfoundation.wallet.onboarding_new_payment.payment_methods

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.payment_result.OnboardingPaymentResultFragmentArgs
import com.asfoundation.wallet.onboarding_new_payment.payment_result.OnboardingPaymentResultSideEffect
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetFirstPaymentMethodsUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetOtherPaymentMethodsUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingPaymentMethodsSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : OnboardingPaymentMethodsSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingPaymentMethodsSideEffect()
}

data class OnboardingPaymentMethodsState(
  val paymentMethodsAsync: Async<List<PaymentMethod>> = Async.Uninitialized,
  val otherPaymentMethods: List<PaymentMethod> = listOf(),
) : ViewState

@HiltViewModel
class OnboardingPaymentMethodsViewModel @Inject constructor(
  private val getFirstPaymentMethodsUseCase: GetFirstPaymentMethodsUseCase,
  private val getOtherPaymentMethodsUseCase: GetOtherPaymentMethodsUseCase, //temporary, to remove later and use getFirstPaymentMethodsUseCase only
  private val cachedTransactionRepository: CachedTransactionRepository,
  private val events: OnboardingPaymentEvents,
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingPaymentMethodsState, OnboardingPaymentMethodsSideEffect>(
    OnboardingPaymentMethodsState()
  ) {

  private var args: OnboardingPaymentMethodsFragmentArgs =
    OnboardingPaymentMethodsFragmentArgs.fromSavedStateHandle(savedStateHandle)

  init {
    handlePaymentMethods()
  }

  //TODO add events to payment start

  private fun handlePaymentMethods() {
    cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        getFirstPaymentMethodsUseCase(cachedTransaction)
          // to only use getFirstPaymentMethodsUseCase and remove the doOnSuccess after all methods are ready
          .doOnSuccess { availablePaymentMethods ->
            setState {
              copy(otherPaymentMethods = getOtherPaymentMethodsUseCase(availablePaymentMethods))
            }
          }
          .asAsyncToState {
            copy(paymentMethodsAsync = it)
          }
      }
      .repeatableScopedSubscribe(OnboardingPaymentMethodsState::paymentMethodsAsync.name)
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { OnboardingPaymentMethodsSideEffect.NavigateToLink(uri) }
  }

  fun handleBackToGameClick() {
    events.sendPaymentMethodEvent(args.transactionBuilder,  null, "back_to_the_game")
    sendSideEffect { OnboardingPaymentMethodsSideEffect.NavigateBackToGame(args.transactionBuilder.domain) }
  }
}