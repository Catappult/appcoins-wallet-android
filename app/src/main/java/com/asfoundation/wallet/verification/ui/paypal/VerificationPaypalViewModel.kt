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
        walletVerificationInteractor.getVerificationStatus(wallet.address, wallet.signedAddress)
      }
      .flatMap { verificationStatus ->
        getVerificationInfoUseCase(AdyenPaymentRepository.Methods.PAYPAL)
          .doOnSuccess { verificationModel ->
            handleVerificationStatus(verificationStatus, verificationModel)
          }
      }
      .doOnError { _uiState.value = VerificationPaypalState.UnknownError }
      .subscribeOn(Schedulers.io())
      .subscribe()
  }

  private fun handleVerificationStatus(
    verificationStatus: VerificationStatus,
    verificationInfo: VerificationIntroModel
  ) {
    cachedPaymentMethod = verificationInfo.paymentInfoModel.paymentMethod
    _uiState.value = when (verificationStatus) {
      CODE_REQUESTED,
      VERIFYING -> VerificationPaypalState.RequestVerificationCode(paymentMethod = verificationInfo.paymentInfoModel.paymentMethod)

      NO_NETWORK -> VerificationPaypalState.UnknownError

      ERROR, VERIFIED, UNVERIFIED -> VerificationPaypalState.ShowVerificationInfo(verificationInfo)
    }
  }

  fun launchVerificationPayment(data: VerificationPaypalData, paymentMethod: ModelObject?) {
    if (paymentMethod != null) {
      makeVerificationPaymentUseCase(VerificationType.PAYPAL, paymentMethod, false, data.returnUrl)
        .subscribeOn(Schedulers.io())
        .doOnSuccess { model ->
          val redirectUrl = model.redirectUrl
          _uiState.value = if (redirectUrl != null)
            VerificationPaypalState.OpenWebPayPalPaymentRequest(redirectUrl)
          else
            VerificationPaypalState.UnknownError
        }
        .subscribe()
    }
  }

  fun successPayment() {
    setCachedVerificationUseCase(VERIFYING)
      .doOnComplete {
        _uiState.value =
          VerificationPaypalState.RequestVerificationCode(paymentMethod = cachedPaymentMethod)
      }
      .doOnError { _uiState.value = VerificationPaypalState.UnknownError }
      .subscribe()
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
      .doOnSubscribe {
        _uiState.value = VerificationPaypalState.RequestVerificationCode(
          loading = true,
          paymentMethod = cachedPaymentMethod
        )
      }
      .subscribe(
        {
          if (it.success) _uiState.value = VerificationPaypalState.VerificationCompleted
          else
            _uiState.value =
              when (it.errorType) {
                WRONG_CODE ->
                  VerificationPaypalState.RequestVerificationCode(
                    wrongCode = true,
                    paymentMethod = cachedPaymentMethod
                  )

                else -> VerificationPaypalState.UnknownError
              }
        },
        { _uiState.value = VerificationPaypalState.Error(it) })

  sealed class VerificationPaypalState {
    object Idle : VerificationPaypalState()

    object Loading : VerificationPaypalState()

    object VerificationCompleted : VerificationPaypalState()

    object UnknownError : VerificationPaypalState()

    data class RequestVerificationCode(
      val wrongCode: Boolean = false,
      val loading: Boolean = false,
      val paymentMethod: ModelObject?
    ) : VerificationPaypalState()

    data class Error(val error: Throwable) : VerificationPaypalState()

    data class OpenWebPayPalPaymentRequest(val url: String) : VerificationPaypalState()

    data class ShowVerificationInfo(val verificationInfo: VerificationIntroModel) :
      VerificationPaypalState()
  }
}
