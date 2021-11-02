package com.asfoundation.wallet.verification.paypal.intro

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.verification.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import io.reactivex.schedulers.Schedulers

sealed class VerificationPaypalIntroSideEffect : SideEffect {
  data class NavigateToPaymentUrl(val url: String) : VerificationPaypalIntroSideEffect()
}

data class VerificationPaypalIntroState(
    val verificationInfoAsync: Async<VerificationIntroModel> = Async.Uninitialized,
    val verificationSubmitAsync: Async<Unit> = Async.Uninitialized
) : ViewState

class VerificationPaypalIntroViewModel(
    private val data: VerificationPaypalIntroData,
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val walletVerificationInteractor: WalletVerificationInteractor,
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
      walletVerificationInteractor.makeVerificationPayment(paymentMethod, false, data.returnUrl)
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
}