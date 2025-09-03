package com.asfoundation.wallet.verification.ui.paypal

import androidx.lifecycle.ViewModel
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationCodeResult.ErrorType.WRONG_CODE
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.CODE_REQUESTED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.ERROR
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.NO_NETWORK
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.UNVERIFIED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.VERIFIED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.VERIFYING
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType.PAYPAL
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.verification.ui.credit_card.VerificationAnalytics
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

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
  private val analytics: VerificationAnalytics
) : ViewModel() {

  private var cachedPaymentMethod: ModelObject? = null

  private val _uiState = MutableStateFlow<VerificationPaypalState>(VerificationPaypalState.Idle)
  var uiState: StateFlow<VerificationPaypalState> = _uiState

  init {
    fetchVerificationStatus()
  }

  fun fetchVerificationStatus() {
    walletService
      .getAndSignCurrentWalletAddress()
      .flatMap { wallet ->
        walletVerificationInteractor.getVerificationStatus(
          address = wallet.address,
          type = PAYPAL
        )
      }
      .flatMap { verificationStatus ->
        getVerificationInfoUseCase(AdyenPaymentRepository.Methods.PAYPAL)
          .doOnSuccess { verificationModel ->
            handleVerificationStatus(verificationStatus, verificationModel)
          }
      }
      .doOnError { showError() }
      .doOnSubscribe { _uiState.value = VerificationPaypalState.Loading }
      .subscribeOn(Schedulers.io())
      .subscribe()
  }

  private fun handleVerificationStatus(
    verificationStatus: VerificationStatus,
    verificationInfo: VerificationIntroModel
  ) {
    cachedPaymentMethod = verificationInfo.paymentInfoModel.paymentMethod
    when (verificationStatus) {
      CODE_REQUESTED,
      VERIFYING -> requestVerificationCode()
      ERROR, VERIFIED, UNVERIFIED -> showVerificationInfo(verificationInfo)
      else -> showVerificationInfo(verificationInfo)
    }
  }

  fun launchVerificationPayment(data: VerificationPaypalData) {
    if (cachedPaymentMethod != null) {
      makeVerificationPaymentUseCase(
        PAYPAL,
        cachedPaymentMethod!!,
        false,
        data.returnUrl
      )
        .subscribeOn(Schedulers.io())
        .doOnSuccess { model ->
          val redirectUrl = model.redirectUrl
          if (redirectUrl != null)
            _uiState.value = VerificationPaypalState.OpenWebPayPalPaymentRequest(redirectUrl)
          else
            showError()
        }
        .subscribe()
    }
  }

  fun successPayment() {
    setCachedVerificationUseCase(VERIFYING, PAYPAL)
      .doOnComplete { requestVerificationCode() }
      .doOnError { showError() }
      .subscribe()
  }

  fun failPayment() {
    showError()
  }

  fun launchChat() {
    displayChatUseCase()
  }

  fun verifyCode(code: String) =
    walletVerificationInteractor
      .confirmVerificationCode(code, PAYPAL)
      .subscribeOn(Schedulers.io())
      .doOnSubscribe {
        _uiState.value = VerificationPaypalState.RequestVerificationCode(
          loading = true
        )
      }
      .subscribe(
        {
          if (it.success) completeVerificationWithSuccess()
          else
            when (it.errorType) {
              WRONG_CODE ->
                _uiState.value = VerificationPaypalState.RequestVerificationCode(
                  wrongCode = true
                )

              else -> showError()
            }
        },
        { _uiState.value = VerificationPaypalState.Error(it) })

  private fun showError() {
    analytics.sendErrorScreenEvent()
    _uiState.value = VerificationPaypalState.UnknownError
  }

  private fun showVerificationInfo(verificationInfo: VerificationIntroModel) {
    analytics.sendInitialScreenEvent()
    _uiState.value = VerificationPaypalState.ShowVerificationInfo(verificationInfo)
  }

  private fun requestVerificationCode() {
    analytics.sendInsertCodeScreenEvent()
    _uiState.value = VerificationPaypalState.RequestVerificationCode()
  }

  private fun completeVerificationWithSuccess() {
    analytics.sendSuccessScreenEvent()
    _uiState.value = VerificationPaypalState.VerificationCompleted
  }

  sealed class VerificationPaypalState {
    object Idle : VerificationPaypalState()

    object Loading : VerificationPaypalState()

    object VerificationCompleted : VerificationPaypalState()

    object UnknownError : VerificationPaypalState()

    data class RequestVerificationCode(
      val wrongCode: Boolean = false,
      val loading: Boolean = false,
    ) : VerificationPaypalState()

    data class Error(val error: Throwable) : VerificationPaypalState()

    data class OpenWebPayPalPaymentRequest(val url: String) : VerificationPaypalState()

    data class ShowVerificationInfo(val verificationInfo: VerificationIntroModel) :
      VerificationPaypalState()
  }
}
