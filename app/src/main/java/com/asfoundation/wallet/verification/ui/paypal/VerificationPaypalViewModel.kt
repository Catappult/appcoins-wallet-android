package com.asfoundation.wallet.verification.ui.paypal

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.CODE_REQUESTED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.NO_NETWORK
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.VERIFYING
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor.VerificationType
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class VerificationPaypalIntroSideEffect : SideEffect

data class VerificationPaypalIntroState(
    val verificationInfoAsync: Async<VerificationIntroModel> = Async.Uninitialized,
    val verificationSubmitAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class VerificationPaypalViewModel
@Inject
constructor(
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase,
    private val setCachedVerificationUseCase: SetCachedVerificationUseCase,
    private val displayChatUseCase: DisplayChatUseCase,
    private val walletVerificationInteractor: WalletVerificationInteractor,
    private val walletService: WalletService,
) : BaseViewModel<VerificationPaypalIntroState, VerificationPaypalIntroSideEffect>(initialState()) {

  private val _uiState = MutableStateFlow<VerificationPaypalState>(VerificationPaypalState.Idle)
  var uiState: StateFlow<VerificationPaypalState> = _uiState

  companion object {
    fun initialState(): VerificationPaypalIntroState = VerificationPaypalIntroState()
  }

  init {
    fetchVerificationStatus()
  }

  private fun fetchVerificationStatus() {
    walletService
        .getAndSignCurrentWalletAddress()
        .flatMap {
          walletVerificationInteractor.getVerificationStatus(it.address, it.signedAddress)
        }
        .doOnSuccess { verificationStatus ->
          when (verificationStatus) {
            CODE_REQUESTED,
            VERIFYING -> _uiState.value = VerificationPaypalState.PaymentCompleted
            NO_NETWORK -> _uiState.value = VerificationPaypalState.UnknownError
            else -> fetchVerificationInfo()
          }
        }
        .doOnError { _uiState.value = VerificationPaypalState.UnknownError }
        .subscribeOn(Schedulers.io())
        .subscribe()
  }

  private fun fetchVerificationInfo() {
    getVerificationInfoUseCase(AdyenPaymentRepository.Methods.PAYPAL)
        .doOnSuccess { _uiState.value = VerificationPaypalState.ShowVerificationInfo(it) }
        .doOnError { _uiState.value = VerificationPaypalState.UnknownError }
        .subscribe()
  }

  fun launchVerificationPayment(data: VerificationPaypalData, paymentMethod: ModelObject?) {
    if (paymentMethod != null) {
      makeVerificationPaymentUseCase(VerificationType.PAYPAL, paymentMethod, false, data.returnUrl)
          .subscribeOn(Schedulers.io())
          .doOnSuccess { model ->
            val redirectUrl = model.redirectUrl
            if (redirectUrl != null) {
              _uiState.value = VerificationPaypalState.NavigateToPaymentUrl(redirectUrl)
            }
          }
          .ignoreElement()
          .asAsyncLoadingToState(VerificationPaypalIntroState::verificationSubmitAsync) { model ->
            copy(verificationSubmitAsync = model)
          }
          .repeatableScopedSubscribe(VerificationPaypalIntroState::verificationSubmitAsync.name) { e
            ->
            e.printStackTrace()
          }
    }
  }

  fun successPayment() {
    setCachedVerificationUseCase(VERIFYING)
        .doOnComplete { _uiState.value = VerificationPaypalState.PaymentCompleted }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  fun failPayment() {
    _uiState.value = VerificationPaypalState.UnknownError
  }

  fun cancelPayment() {
    _uiState.value = VerificationPaypalState.Error(Throwable(WebViewActivity.USER_CANCEL_THROWABLE))
  }

  fun launchChat() {
    displayChatUseCase()
  }

  fun verifyCode(code: String) =
      walletVerificationInteractor
          .confirmVerificationCode(code)
          .subscribeOn(Schedulers.io())
          .subscribe(
              {
                if (it.success) _uiState.value = VerificationPaypalState.Success
                else
                    when (it.errorType) {
                      VerificationCodeResult.ErrorType.WRONG_CODE ->
                          _uiState.value = VerificationPaypalState.PaymentCompleted
                      VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS -> fetchVerificationInfo()
                      else -> _uiState.value = VerificationPaypalState.UnknownError
                    }
              },
              { _uiState.value = VerificationPaypalState.Error(it) })

  sealed class VerificationPaypalState {
    object Idle : VerificationPaypalState()

    object Loading : VerificationPaypalState()

    object PaymentCompleted : VerificationPaypalState()

    object Success : VerificationPaypalState()

    object UnknownError : VerificationPaypalState()

    data class Error(val error: Throwable) : VerificationPaypalState()

    data class NavigateToPaymentUrl(val url: String) : VerificationPaypalState()

    data class ShowVerificationInfo(val verificationInfo: VerificationIntroModel) :
        VerificationPaypalState()
  }
}
