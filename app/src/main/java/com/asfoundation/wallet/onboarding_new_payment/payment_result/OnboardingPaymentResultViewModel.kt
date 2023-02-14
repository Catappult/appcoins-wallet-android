package com.asfoundation.wallet.onboarding_new_payment.payment_result

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.mapToService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
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
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingPaymentResultState, OnboardingPaymentResultSideEffect>(
    OnboardingPaymentResultState
  ) {

  private var args: OnboardingPaymentResultFragmentArgs =
    OnboardingPaymentResultFragmentArgs.fromSavedStateHandle(savedStateHandle)

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
        when (args.paymentModel.error.errorInfo?.errorType) {
          ErrorInfo.ErrorType.BILLING_ADDRESS -> {
            //TODO handle billing address error
          }
          else -> {
            events.sendPaymentErrorEvent(args.transactionBuilder, args.paymentType)
            sendSideEffect { OnboardingPaymentResultSideEffect.ShowPaymentError(args.paymentModel.error) }
          }
        }
      }
      args.paymentModel.status == PaymentModel.Status.CANCELED -> {
        sendSideEffect { OnboardingPaymentResultSideEffect.NavigateBackToPaymentMethods }
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
            events.sendPaymentSuccessEvent(args.transactionBuilder, args.paymentType)
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
          isPaymentFailed(authorisedPaymentModel.status) -> {
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
                authorisedPaymentModel.error
              )
            }
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
                authorisedPaymentModel.error
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
    events.sendPaymentErrorEvent(
      args.transactionBuilder,
      args.paymentType,
      args.paymentModel.refusalCode,
      args.paymentModel.refusalReason,
      riskRules
    )
  }

  private fun handleFraudFlow(error: Error, refusalCode: Int) {
    adyenPaymentInteractor.isWalletVerified()
      .doOnSuccess { verified ->
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(error, refusalCode, verified)
        }

      }.doOnError {
        sendSideEffect {
          OnboardingPaymentResultSideEffect.ShowPaymentError(error, refusalCode)
        }
      }.scopedSubscribe()
  }

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == PaymentModel.Status.FAILED || status == PaymentModel.Status.CANCELED || status == PaymentModel.Status.INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

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

  fun handleBackToGameClick() {
    events.sendBackToTheGameEvent("payment_result")
    sendSideEffect { OnboardingPaymentResultSideEffect.NavigateBackToGame(args.transactionBuilder.domain) }
  }

  fun handleExploreWalletClick() {
    events.sendExploreWalletEvent("payment_result")
    sendSideEffect { OnboardingPaymentResultSideEffect.NavigateToExploreWallet }
  }

  fun showSupport(gamificationLevel: Int) {
    supportInteractor.showSupport(gamificationLevel)
      .scopedSubscribe()
  }
}