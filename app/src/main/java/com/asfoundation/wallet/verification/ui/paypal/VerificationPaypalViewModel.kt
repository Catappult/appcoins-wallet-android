package com.asfoundation.wallet.verification.ui.paypal

import androidx.lifecycle.ViewModel
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
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
      VERIFYING -> showCheckEmail()
      // Already verified: start a new verification like before (e.g. a different PayPal account).
      // Completing a pending one is handled by refreshEmailVerification().
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
      .doOnComplete { showCheckEmail() }
      .doOnError { showError() }
      .subscribe()
  }

  fun failPayment() {
    showError()
  }

  fun launchChat() {
    displayChatUseCase()
  }

  /**
   * PayPal verifications are completed through the link the broker emails, outside the app,
   * so while [VerificationPaypalState.CheckEmail] is shown we ask the server for the outcome.
   */
  fun refreshEmailVerification() {
    if (_uiState.value != VerificationPaypalState.CheckEmail) return
    walletService
      .getAndSignCurrentWalletAddress()
      .flatMap { wallet ->
        walletVerificationInteractor.getVerificationStatus(address = wallet.address, type = PAYPAL)
      }
      .subscribeOn(Schedulers.io())
      .subscribe({ status ->
        if (_uiState.value != VerificationPaypalState.CheckEmail) return@subscribe
        when (status) {
          VERIFIED -> completeVerificationWithSuccess()
          UNVERIFIED -> showError() // canceled, expired, failed or not found
          else -> Unit // still pending, or offline: keep waiting
        }
      }, {})
  }

  private fun showError() {
    analytics.sendErrorScreenEvent()
    _uiState.value = VerificationPaypalState.UnknownError
  }

  private fun showVerificationInfo(verificationInfo: VerificationIntroModel) {
    analytics.sendInitialScreenEvent()
    _uiState.value = VerificationPaypalState.ShowVerificationInfo(verificationInfo)
  }

  private fun showCheckEmail() {
    analytics.sendInsertCodeScreenEvent()
    _uiState.value = VerificationPaypalState.CheckEmail
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

    object CheckEmail : VerificationPaypalState()

    data class Error(val error: Throwable) : VerificationPaypalState()

    data class OpenWebPayPalPaymentRequest(val url: String) : VerificationPaypalState()

    data class ShowVerificationInfo(val verificationInfo: VerificationIntroModel) :
      VerificationPaypalState()
  }
}
