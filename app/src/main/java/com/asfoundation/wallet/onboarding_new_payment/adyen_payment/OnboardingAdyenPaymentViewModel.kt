package com.asfoundation.wallet.onboarding_new_payment.adyen_payment

import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.mapToService
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentInfoModelUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionOriginUseCase
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.BillingWebViewFragment
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

sealed class OnboardingAdyenPaymentSideEffect : SideEffect {
  data class NavigateToPaymentResult(val paymentModel: PaymentModel) :
    OnboardingAdyenPaymentSideEffect()

  data class NavigateToPaypal(val redirectUrl: String) : OnboardingAdyenPaymentSideEffect()
  data class HandleWebViewResult(val uri: Uri) : OnboardingAdyenPaymentSideEffect()
  object ShowLoading : OnboardingAdyenPaymentSideEffect()
  object ShowCvvError : OnboardingAdyenPaymentSideEffect()
  object NavigateBackToPaymentMethods : OnboardingAdyenPaymentSideEffect()
}

data class OnboardingAdyenPaymentState(val paymentInfoModel: Async<PaymentInfoModel> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class OnboardingAdyenPaymentViewModel @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val events: OnboardingPaymentEvents,
  private val getPaymentInfoModelUseCase: GetPaymentInfoModelUseCase,
  private val transactionOriginUseCase: GetTransactionOriginUseCase,
  private val supportInteractor: SupportInteractor,
  private val savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingAdyenPaymentState, OnboardingAdyenPaymentSideEffect>(
    OnboardingAdyenPaymentState()
  ) {

  private lateinit var args: OnboardingAdyenPaymentFragmentArgs
  private val tempReferrerUrl =
    "https://apichain.dev.catappult.io/transaction/inapp?product=antifreeze&value=1.5&currency=USD&callback_url=https%3A%2F%2Fapi.dev.catappult.io%2Fbroker%2F8.20200101%2Fmock%2Fcallback&domain=com.appcoins.trivialdrivesample.test&signature=7878cb314b82ad2684ad4865cf84ab33e2905d2b6c7f9c3a368f6f70917e1364"

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

  fun handleBuyClick(adyenCard: AdyenCardWrapper, returnUrl: String) {
    sendSideEffect { OnboardingAdyenPaymentSideEffect.ShowLoading }
    transactionOriginUseCase(args.transactionBuilder)
      .flatMap { origin ->
        events.sendPaymentConfirmationEvent(args.transactionBuilder, args.paymentType)
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
          referrerUrl = tempReferrerUrl
        )
      }
      .doOnSuccess { paymentModel ->
        handlePaymentResult(paymentModel)
      }.scopedSubscribe()
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
        events.sendPaymentErrorEvent(
          args.transactionBuilder,
          args.paymentType,
          paymentModel.refusalCode,
          paymentModel.refusalReason
        )
      }
    }
    sendSideEffect {
      OnboardingAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
    }
  }

  fun handleBackButton() {
    events.sendPaymentMethodEvent(args.transactionBuilder, args.paymentType)
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
          referrerUrl = tempReferrerUrl
        )
      }
      .doOnSuccess { paymentModel ->
        handlePaypalResult(paymentModel)
      }
      .scopedSubscribe()
  }

  private fun handlePaypalResult(paymentModel: PaymentModel) {
    paymentModel.redirectUrl?.let { redirectUrl ->
      sendSideEffect {
        OnboardingAdyenPaymentSideEffect.NavigateToPaypal(redirectUrl)
      }
    }
  }

  fun handleWebViewResult(result: ActivityResult) {
    when (result.resultCode) {
      WebViewActivity.FAIL -> {
        when {
          result.data?.dataString?.contains("codapayments") != true -> {
            if (result.data?.dataString?.contains(
                BillingWebViewFragment.CARRIER_BILLING_ONE_BIP_SCHEMA
              ) == true
            ) {
              events.sendCarrierBillingConfirmationEvent(args.transactionBuilder, "cancel")
            } else {
              events.sendPayPalConfirmationEvent(args.transactionBuilder, "cancel")
            }
          }
          result.data?.dataString?.contains(BillingWebViewFragment.OPEN_SUPPORT) == true -> {
            supportInteractor.displayChatScreen()
          }
        }
        sendSideEffect { OnboardingAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
      }
      WebViewActivity.SUCCESS -> {
        if (result.data?.scheme?.contains("adyencheckout") == true) {
          events.sendPaypalUrlEvent(args.transactionBuilder, result.data!!)
          if (events.getQueryParameter(result.data!!, "resultCode") == "cancelled")
            events.sendPayPalConfirmationEvent(args.transactionBuilder, "cancel")
          else
            events.sendPayPalConfirmationEvent(args.transactionBuilder, "buy")
        }
        sendSideEffect {
          result.data!!.data?.let { uri ->
            OnboardingAdyenPaymentSideEffect.HandleWebViewResult(uri)
          }
        }
      }
    }
  }

  fun handleRedirectComponentResponse(actionComponentData: ActionComponentData) {
//    adyenPaymentInteractor.submitRedirect(
//      uid = cachedUid,
//      details = convertToJson(actionComponentData.details!!),
//      paymentData = actionComponentData.paymentData ?: cachedPaymentData
//    ).doOnSuccess { paymentModel ->
//      handlePaymentResult(paymentModel)
//    }.scopedSubscribe()
  }

  //This method is used to avoid the nameValuePairs key problem that occurs when we pass a JSONObject trough a GSON converter
  private fun convertToJson(details: JSONObject): JsonObject {
    val json = JsonObject()
    val keys = details.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val value = details.get(key)
      if (value is String) json.addProperty(key, value)
    }
    return json
  }

  fun handle3DSErrors(componentError: ComponentError) {
    if (componentError.errorMessage == "Challenge canceled.") {
      events.send3dsCancel()
      sendSideEffect { OnboardingAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
    } else {
      events.send3dsError(componentError.errorMessage)
    }
  }
}