package com.asfoundation.wallet.verification.ui.paypal

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import io.reactivex.schedulers.Schedulers

sealed class VerificationPaypalIntroSideEffect : SideEffect {
  data class NavigateToPaymentUrl(val url: String) : VerificationPaypalIntroSideEffect()
}

data class VerificationPaypalIntroState(
    val verificationInfoAsync: Async<VerificationIntroModel> = Async.Uninitialized,
    val verificationSubmitAsync: Async<Unit> = Async.Uninitialized
) : ViewState

class VerificationPaypalIntroViewModel(
    private val data: VerificationPaypalData,
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase,
) : BaseViewModel<VerificationPaypalIntroState, VerificationPaypalIntroSideEffect>(initialState()) {

  companion object {
    fun initialState(): VerificationPaypalIntroState = VerificationPaypalIntroState()
  }

  init {
    fetchVerificationInfo()
  }

  private fun fetchVerificationInfo() {
    getVerificationInfoUseCase(AdyenPaymentRepository.Methods.PAYPAL)
        .asAsyncToState(VerificationPaypalIntroState::verificationInfoAsync) {
          copy(verificationInfoAsync = it)
        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  fun launchVerificationPayment() {
    val paymentMethod = state.verificationInfoAsync.value?.paymentInfoModel?.paymentMethodInfo
    if (paymentMethod != null) {
      makeVerificationPaymentUseCase(WalletVerificationInteractor.VerificationType.PAYPAL,
          paymentMethod, false, data.returnUrl)
          .subscribeOn(Schedulers.io())
          .doOnSuccess { model ->
            val redirectUrl = model.redirectUrl
            if (redirectUrl != null) {
              sendSideEffect { VerificationPaypalIntroSideEffect.NavigateToPaymentUrl(redirectUrl) }
            }
          }
          .ignoreElement()
          .asAsyncLoadingToState(VerificationPaypalIntroState::verificationSubmitAsync) { model ->
            copy(verificationSubmitAsync = model)
          }
          .repeatableScopedSubscribe(
              VerificationPaypalIntroState::verificationSubmitAsync.name) { e -> e.printStackTrace() }
    }
  }

  fun successPayment() {
    setState { copy(verificationSubmitAsync = Async.Success(Unit)) }
  }

  fun failPayment() {
    setState { copy(verificationSubmitAsync = Async.Fail(Error.UnknownError(Throwable("")))) }
  }

  fun tryAgain() {
    setState { copy(verificationSubmitAsync = Async.Uninitialized) }
  }
}