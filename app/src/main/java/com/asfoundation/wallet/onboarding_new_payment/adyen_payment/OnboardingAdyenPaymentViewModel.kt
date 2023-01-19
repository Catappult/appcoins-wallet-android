package com.asfoundation.wallet.onboarding_new_payment.adyen_payment

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.onboarding_new_payment.mapToService
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentInfoModelUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionOriginUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject

sealed class OnboardingAdyenPaymentSideEffect : SideEffect {
  data class NavigateToPaymentResult(val paymentModel: PaymentModel) :
    OnboardingAdyenPaymentSideEffect()

  object ShowLoading : OnboardingAdyenPaymentSideEffect()
  object ShowCvvError : OnboardingAdyenPaymentSideEffect()
  object NavigateBackToPaymentMethods : OnboardingAdyenPaymentSideEffect()
}

data class OnboardingAdyenPaymentState(val paymentInfoModel: Async<PaymentInfoModel> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class OnboardingAdyenPaymentViewModel @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val paymentMethodsAnalytics: PaymentMethodsAnalytics,
  private val billingAnalytics: BillingAnalytics,
  private val getPaymentInfoModelUseCase: GetPaymentInfoModelUseCase,
  private val transactionOriginUseCase: GetTransactionOriginUseCase,
  private val savedStateHandle: SavedStateHandle,
  private val rxSchedulers: RxSchedulers
) :
  BaseViewModel<OnboardingAdyenPaymentState, OnboardingAdyenPaymentSideEffect>(
    OnboardingAdyenPaymentState()
  ) {

  private lateinit var args: OnboardingAdyenPaymentFragmentArgs

  init {
    getSavedStateArguments()

    handlePaymentInfo()
  }

  private fun getSavedStateArguments() {
    args = OnboardingAdyenPaymentFragmentArgs.fromSavedStateHandle(savedStateHandle)
  }

  private fun handlePaymentInfo() {
    getPaymentInfoModelUseCase(
      paymentType = args.paymentType.toString(),
      value = args.amount,
      currency = args.currency
    ).asAsyncToState {
      copy(paymentInfoModel = it)
    }.scopedSubscribe()
  }

  fun handleBuyClick(paymentDataSubject: ReplaySubject<AdyenCardWrapper>, returnUrl: String) {
    sendSideEffect { OnboardingAdyenPaymentSideEffect.ShowLoading }
    paymentDataSubject.firstOrError()
      .observeOn(rxSchedulers.io)
      .flatMap { adyenCard ->
        transactionOriginUseCase(args.transactionBuilder)
          .flatMap { origin ->
            handleBuyAnalytics()
            adyenPaymentInteractor.makePayment(
              adyenPaymentMethod = adyenCard.cardPaymentMethod,
              shouldStoreMethod = adyenCard.shouldStoreCard,
              hasCvc = adyenCard.hasCvc,
              supportedShopperInteraction = adyenCard.supportedShopperInteractions,
              returnUrl = returnUrl,
              value = args.amount,
              currency = args.currency,
              reference = args.transactionBuilder.orderReference,
              paymentType = args.paymentType.mapToService().transactionType,
              origin = origin,
              packageName = args.transactionBuilder.domain,
              metadata = args.transactionBuilder.payload,
              sku = args.transactionBuilder.skuId,
              callbackUrl = args.transactionBuilder.callbackUrl,
              transactionType = args.transactionBuilder.type,
              developerWallet = args.transactionBuilder.toAddress(),
              referrerUrl = args.transactionBuilder.referrerUrl
            )
          }
      }.doOnSuccess { paymentModel ->
        handlePaymentResult(paymentModel)
      }.scopedSubscribe()
  }

  private fun handleBuyAnalytics() {
    billingAnalytics.sendPaymentConfirmationEvent(
      args.transactionBuilder.domain,
      args.transactionBuilder.skuId,
      args.transactionBuilder.amount().toString(),
      args.paymentType.mapToService().transactionType,
      args.transactionBuilder.type,
      "buy"
    )
  }

  private fun handlePaymentResult(paymentModel: PaymentModel) {
    when {
      paymentModel.refusalReason != null -> {
        paymentModel.refusalCode?.let { code ->
          when (code) {
            AdyenErrorCodeMapper.CVC_DECLINED -> sendSideEffect { OnboardingAdyenPaymentSideEffect.ShowCvvError }
            else -> sendSideEffect {
              OnboardingAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
            }
          }
        }
        sendPaymentErrorEvent(paymentModel.refusalCode, paymentModel.refusalReason)
      }
    }
    sendSideEffect {
      OnboardingAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
    }
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

  fun handleBackButton() {
    paymentMethodsAnalytics.sendPaymentMethodEvent(
      args.transactionBuilder.domain,
      args.transactionBuilder.skuId,
      args.transactionBuilder.amount().toString(),
      args.paymentType.mapToService().transactionType,
      args.transactionBuilder.type,
      "other_payments"
    )
    sendSideEffect { OnboardingAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
  }

  fun handlePaypal(paymentInfoModel: PaymentInfoModel, returnUrl: String) {
    transactionOriginUseCase(args.transactionBuilder)
      .flatMap { origin ->
        adyenPaymentInteractor.makePayment(
          adyenPaymentMethod = paymentInfoModel.paymentMethod!!,
          shouldStoreMethod = false,
          hasCvc = false,
          supportedShopperInteraction = emptyList(),
          returnUrl = returnUrl,
          value = args.amount,
          currency = args.currency,
          reference = args.transactionBuilder.orderReference,
          paymentType = args.paymentType.mapToService().transactionType,
          origin = origin,
          packageName = args.transactionBuilder.domain,
          metadata = args.transactionBuilder.payload,
          sku = args.transactionBuilder.skuId,
          callbackUrl = args.transactionBuilder.callbackUrl,
          transactionType = args.transactionBuilder.type,
          developerWallet = args.transactionBuilder.toAddress(),
          referrerUrl = args.transactionBuilder.referrerUrl
        )
      }
      .doOnSuccess { paymentModel ->
        handlePaypalResult(paymentModel)
      }
      .scopedSubscribe()
  }

  private fun handlePaypalResult(paymentModel: PaymentModel) {
    sendSideEffect {
      OnboardingAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
    }
  }
}