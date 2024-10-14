package com.asfoundation.wallet.onboarding_new_payment.payment_result

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CANCELLED_DUE_TO_FRAUD
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.ISSUER_SUSPECTED_FRAUD
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.adyen.PaymentType.CARD
import com.asfoundation.wallet.billing.adyen.PaymentType.PAYPAL
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.onboarding.use_cases.GetResponseCodeWebSocketUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetResponseCodeWebSocketUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents.Companion.BACK_TO_THE_GAME
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents.Companion.EXPLORE_WALLET
import com.asfoundation.wallet.onboarding_new_payment.mapToService
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import javax.inject.Inject

sealed class OnboardingPaymentResultSideEffect : SideEffect {
  data class ShowPaymentError(
    val error: Error? = null,
    val refusalCode: Int? = null,
    val isWalletVerified: Boolean? = null,
    val paymentType: PaymentType
  ) : OnboardingPaymentResultSideEffect()

  data class ShowPaymentSuccess(val purchaseBundleModel: PurchaseBundleModel) :
    OnboardingPaymentResultSideEffect()

  data class NavigateBackToGame(val appPackageName: String) : OnboardingPaymentResultSideEffect()
  object NavigateToExploreWallet : OnboardingPaymentResultSideEffect()
  object NavigateBackToPaymentMethods : OnboardingPaymentResultSideEffect()
}

object OnboardingPaymentResultState : ViewState

@HiltViewModel
class OnboardingPaymentResultViewModel @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val events: OnboardingPaymentEvents,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val supportInteractor: SupportInteractor,
  private val rxSchedulers: RxSchedulers,
  private val setResponseCodeWebSocketUseCase: SetResponseCodeWebSocketUseCase,
  private val getResponseCodeWebSocketUseCase: GetResponseCodeWebSocketUseCase,
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingPaymentResultState, OnboardingPaymentResultSideEffect>(
    OnboardingPaymentResultState
  ) {

  private var args: OnboardingPaymentResultFragmentArgs =
    OnboardingPaymentResultFragmentArgs.fromSavedStateHandle(savedStateHandle)

  private var uid: String? = null

  init {
    handlePaymentResult()
  }

  private fun handlePaymentResult() {
    when {
      args.paymentModel.resultCode.equals("AUTHORISED", true) -> {
        handleAuthorisedPayment()
      }

      args.paymentModel.refusalCode != null -> {
        handlePaymentRefusal()
      }

      args.paymentModel.error.hasError -> {
        events.sendPaymentErrorEvent(args.transactionBuilder, args.paymentType)
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            args.paymentModel.error,
            paymentType = args.paymentType
          )
        }
      }

      args.paymentModel.status == PaymentModel.Status.CANCELED -> {
        sendSideEffect { OnboardingPaymentResultSideEffect.NavigateBackToPaymentMethods }
      }

      else -> {
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            args.paymentModel.error,
            paymentType = args.paymentType
          )
        }
      }
    }
  }

  private fun handleAuthorisedPayment() {
    adyenPaymentInteractor.getAuthorisedTransaction(args.paymentModel.uid)
      .doOnNext { authorisedPaymentModel ->
        when (authorisedPaymentModel.status) {
          PaymentModel.Status.COMPLETED -> {
            args.paymentModel.purchaseUid = authorisedPaymentModel.purchaseUid
            uid = authorisedPaymentModel.uid
            args.paymentModel.hash = authorisedPaymentModel.hash
            events.sendPaymentSuccessEvent(
              args.transactionBuilder,
              args.paymentType,
              authorisedPaymentModel.uid
            )
            createBundle(
              authorisedPaymentModel.hash,
              authorisedPaymentModel.orderReference,
              authorisedPaymentModel.purchaseUid
            ).map { purchaseBundleModel ->
              events.sendPaymentSuccessFinishEvents(args.transactionBuilder, args.paymentType)
              sendSideEffect {
                setOnboardingCompletedUseCase()
                OnboardingPaymentResultSideEffect.ShowPaymentSuccess(
                  purchaseBundleModel
                )
              }
            }.subscribe()
          }

          else -> {
            events.sendPaymentErrorEvent(
              args.transactionBuilder,
              args.paymentType,
              authorisedPaymentModel.error.errorInfo?.httpCode,
              buildRefusalReason(
                authorisedPaymentModel.status,
                authorisedPaymentModel.error.errorInfo?.text
              )
            )
            sendSideEffect {
              OnboardingPaymentResultSideEffect.ShowPaymentError(
                authorisedPaymentModel.error,
                paymentType = args.paymentType
              )
            }
          }
        }
      }.scopedSubscribe()
  }

  private fun handlePaymentRefusal() {
    var riskRules: String? = null
    args.paymentModel.refusalCode?.let { code ->
      when (code) {
        FRAUD, ISSUER_SUSPECTED_FRAUD, CANCELLED_DUE_TO_FRAUD -> {
          handleFraudFlow(args.paymentModel.error, code)
          riskRules = args.paymentModel.fraudResultIds.sorted().joinToString(separator = "-")
        }

        else -> sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            args.paymentModel.error,
            code,
            paymentType = args.paymentType
          )
        }
      }
    }
    events.sendPaymentErrorEvent(
      args.transactionBuilder,
      args.paymentType,
      args.paymentModel.refusalCode,
      args.paymentModel.refusalReason,
      riskRules
    )
  }

  fun handleFraudFlow(error: Error, refusalCode: Int) {
    val verificationType = if (args.paymentType == CARD) {
      VerificationType.CREDIT_CARD
    } else {
      VerificationType.PAYPAL
    }
    adyenPaymentInteractor.isWalletVerified(verificationType)
      .doOnSuccess { verified ->
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            refusalCode = refusalCode,
            isWalletVerified = verified,
            paymentType = args.paymentType
          )
        }

      }.doOnError {
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            error,
            refusalCode,
            paymentType = args.paymentType
          )
        }
      }.scopedSubscribe()
  }

  private fun buildRefusalReason(status: PaymentModel.Status, message: String?): String =
    message?.let { "$status : $it" } ?: status.toString()

  private fun createBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?
  ): Single<PurchaseBundleModel> {
    return adyenPaymentInteractor
      .getCompletePurchaseBundle(
        args.transactionBuilder.type,
        args.transactionBuilder.domain,
        args.transactionBuilder.skuId,
        purchaseUid,
        orderReference,
        hash,
        rxSchedulers.io
      )
      .map { mapPaymentMethodId(it) }
      .subscribeOn(rxSchedulers.io)
  }


  private fun mapPaymentMethodId(purchaseBundleModel: PurchaseBundleModel): PurchaseBundleModel {
    val bundle = purchaseBundleModel.bundle
    if (args.paymentType == CARD || args.paymentType == PAYPAL) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        args.paymentType.mapToService().transactionType
      )
    }
    return PurchaseBundleModel(bundle, purchaseBundleModel.renewal)
  }

  fun handleBackToGameClick() {
    events.sendPaymentConclusionNavigationEvent(BACK_TO_THE_GAME)
    sendSideEffect { OnboardingPaymentResultSideEffect.NavigateBackToGame(args.transactionBuilder.domain) }
  }

  fun handleExploreWalletClick() {
    events.sendPaymentConclusionNavigationEvent(EXPLORE_WALLET)
    sendSideEffect { OnboardingPaymentResultSideEffect.NavigateToExploreWallet }
  }

  fun showSupport(gamificationLevel: Int) {
    supportInteractor.showSupport(gamificationLevel, uid)
      .scopedSubscribe()
  }

  fun setResponseCodeWebSocket(responseCode: Int) = setResponseCodeWebSocketUseCase(responseCode)


  fun getResponseCodeWebSocket() = getResponseCodeWebSocketUseCase()

}