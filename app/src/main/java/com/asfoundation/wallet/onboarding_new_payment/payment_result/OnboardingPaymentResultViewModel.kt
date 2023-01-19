package com.asfoundation.wallet.onboarding_new_payment.payment_result

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.adyen.*
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.onboarding_new_payment.mapToService
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetAnalyticsRevenueValueUseCase
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import javax.inject.Inject

sealed class OnboardingPaymentResultSideEffect : SideEffect {
  data class ShowPaymentError(
    val error: Error? = null,
    val refusalCode: Int? = null,
    val isWalletVerified: Boolean? = null
  ) : OnboardingPaymentResultSideEffect()

  data class ShowPaymentSuccess(val purchaseBundleModel: PurchaseBundleModel) :
    OnboardingPaymentResultSideEffect()
}

object OnboardingPaymentResultState : ViewState

@HiltViewModel
class OnboardingPaymentResultViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val analyticsRevenueUseCase: GetAnalyticsRevenueValueUseCase,
  private val paymentMethodsAnalytics: PaymentMethodsAnalytics,
  private val billingAnalytics: BillingAnalytics,
  private val rxSchedulers: RxSchedulers
) :
  BaseViewModel<OnboardingPaymentResultState, OnboardingPaymentResultSideEffect>(
    OnboardingPaymentResultState
  ) {

  private lateinit var args: OnboardingPaymentResultFragmentArgs

  init {
    getSavedStateArguments()
    handlePaymentResult()
  }

  private fun getSavedStateArguments() {
    args = OnboardingPaymentResultFragmentArgs.fromSavedStateHandle(savedStateHandle)
  }

  private fun handlePaymentResult() {
    when {
      args.paymentModel.resultCode.equals("AUTHORISED", true) -> {
        handleAuthorisedPayment()
      }
      args.paymentModel.refusalReason != null -> {
        handlePaymentRefusal()
      }
      args.paymentModel.status == PaymentModel.Status.PENDING_USER_PAYMENT && args.paymentModel.action != null -> {
        handleAdyenAction(args.paymentModel)
      }
      else -> {
        sendSideEffect { OnboardingPaymentResultSideEffect.ShowPaymentError(args.paymentModel.error) }
      }
    }
  }

  private fun handleAuthorisedPayment() {
    adyenPaymentInteractor.getAuthorisedTransaction(args.paymentModel.uid)
      .doOnNext { authorisedPaymentModel ->
        when {
          authorisedPaymentModel.status == PaymentModel.Status.COMPLETED -> {
            sendPaymentSuccessEvent()
            createBundle(
              authorisedPaymentModel.hash,
              authorisedPaymentModel.orderReference,
              authorisedPaymentModel.purchaseUid
            ).doOnSuccess { purchaseBundleModel ->
              sendPaymentSuccessFinishEvents()
              sendSideEffect {
                OnboardingPaymentResultSideEffect.ShowPaymentSuccess(
                  purchaseBundleModel
                )
              }
            }.subscribe()
          }
          isPaymentFailed(authorisedPaymentModel.status) -> {
            sendPaymentErrorEvent(
              authorisedPaymentModel.error.errorInfo?.httpCode,
              buildRefusalReason(
                authorisedPaymentModel.status,
                authorisedPaymentModel.error.errorInfo?.text
              )
            )
            sendSideEffect {
              OnboardingPaymentResultSideEffect.ShowPaymentError(
                authorisedPaymentModel.error
              )
            }
          }
          else -> {
            sendPaymentErrorEvent(
              authorisedPaymentModel.error.errorInfo?.httpCode,
              buildRefusalReason(
                authorisedPaymentModel.status,
                authorisedPaymentModel.error.errorInfo?.text
              )
            )
            sendSideEffect {
              OnboardingPaymentResultSideEffect.ShowPaymentError(
                authorisedPaymentModel.error
              )
            }
          }
        }
      }
  }

  private fun handlePaymentRefusal() {
    var riskRules: String? = null
    args.paymentModel.refusalCode?.let { code ->
      when (code) {
        AdyenErrorCodeMapper.FRAUD -> {
          handleFraudFlow(args.paymentModel.error, code)
          riskRules = args.paymentModel.fraudResultIds.sorted().joinToString(separator = "-")
        }
        else -> sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(
            args.paymentModel.error,
            code
          )
        }
      }
    }
    sendPaymentErrorEvent(
      args.paymentModel.refusalCode,
      args.paymentModel.refusalReason,
      riskRules
    )
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
//    if (paymentModel.action != null) {
//      when (val type = paymentModel.action?.type) {
//        REDIRECT -> {
//          action3ds = type
//          paymentMethodsAnalytics.send3dsStart(action3ds)
//          cachedPaymentData = paymentModel.paymentData
//          cachedUid = paymentModel.uid
//          navigator.navigateToUriForResult(paymentModel.redirectUrl)
//          waitingResult = true
//        }
//        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
//          action3ds = type
//          paymentMethodsAnalytics.send3dsStart(action3ds)
//          cachedUid = paymentModel.uid
//          view.handle3DSAction(paymentModel.action!!)
//          waitingResult = true
//        }
//        else -> {
//          sendSideEffect { OnboardingPaymentResultSideEffect.ShowPaymentError() }
//        }
//      }
//    }
  }

  private fun handleFraudFlow(error: Error, refusalCode: Int) {
    adyenPaymentInteractor.isWalletVerified()
      .doOnSuccess { verified ->
        OnboardingPaymentResultSideEffect.ShowPaymentError(error, refusalCode, verified)
      }.doOnError {
        OnboardingPaymentResultSideEffect.ShowPaymentError(error, refusalCode)
      }.scopedSubscribe()
  }

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == PaymentModel.Status.FAILED || status == PaymentModel.Status.CANCELED || status == PaymentModel.Status.INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

  private fun buildRefusalReason(status: PaymentModel.Status, message: String?): String =
    message?.let { "$status : $it" } ?: status.toString()

  private fun sendPaymentSuccessEvent() {
    billingAnalytics.sendPaymentSuccessEvent(
      args.transactionBuilder.domain,
      args.transactionBuilder.skuId,
      args.transactionBuilder.amount().toString(),
      args.paymentType.mapToService().transactionType,
      args.transactionBuilder.type
    )
  }

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
    if (args.paymentType == PaymentType.CARD) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        args.paymentType.mapToService().transactionType
      )
    } else if (args.paymentType == PaymentType.PAYPAL) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        args.paymentType.mapToService().transactionType
      )
    }
    return PurchaseBundleModel(bundle, purchaseBundleModel.renewal)
  }

  private fun sendPaymentSuccessFinishEvents() {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = args.paymentType.mapToService().transactionType,
      success = true,
      isPreselected = false
    )
    billingAnalytics.sendPaymentEvent(
      args.transactionBuilder.domain,
      args.transactionBuilder.skuId,
      args.transactionBuilder.amount().toString(),
      args.paymentType.mapToService().transactionType,
      args.transactionBuilder.type
    )
    billingAnalytics.sendRevenueEvent(analyticsRevenueUseCase(args.transactionBuilder))
  }

  private fun sendPaymentErrorEvent(
    refusalCode: Int?,
    refusalReason: String?,
    riskRules: String? = null
  ) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = args.paymentType.mapToService().transactionType,
      success = false,
      isPreselected = false
    )
    billingAnalytics.sendPaymentErrorWithDetailsAndRiskEvent(
      args.transactionBuilder.domain,
      args.transactionBuilder.skuId,
      args.transactionBuilder.amount().toString(),
      args.paymentType.mapToService().transactionType,
      args.transactionBuilder.type,
      refusalCode.toString(),
      refusalReason,
      riskRules
    )
  }
}