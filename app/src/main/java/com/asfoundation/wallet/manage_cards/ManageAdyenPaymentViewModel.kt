package com.asfoundation.wallet.manage_cards

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentInfoModelUseCase
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

sealed class ManageAdyenPaymentSideEffect : SideEffect {
  data class NavigateToPaymentResult(val paymentModel: PaymentModel) :
    ManageAdyenPaymentSideEffect()

  data class Handle3DS(val action: Action?) : ManageAdyenPaymentSideEffect()
  object ShowLoading : ManageAdyenPaymentSideEffect()
  object ShowCvvError : ManageAdyenPaymentSideEffect()
  object NavigateBackToPaymentMethods : ManageAdyenPaymentSideEffect()
}

data class ManageAdyenPaymentState(val paymentInfoModel: Async<PaymentInfoModel> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class ManageAdyenPaymentViewModel @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val getPaymentInfoModelUseCase: GetPaymentInfoModelUseCase,
  private val rxSchedulers: RxSchedulers,
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<ManageAdyenPaymentState, ManageAdyenPaymentSideEffect>(
    ManageAdyenPaymentState()
  ) {

  private lateinit var cachedUid: String

  init {
    handlePaymentInfo()
  }

  private fun handlePaymentInfo() {
//    events.sendPaymentMethodEvent(
//      args.transactionBuilder,
//      args.paymentType,
//      BillingAnalytics.ACTION_BUY
//    )
    getPaymentInfoModelUseCase(
      paymentType = AdyenPaymentRepository.Methods.CREDIT_CARD.transactionType,
      value = "0",
      currency = "EUR"
    ).asAsyncToState {
      copy(paymentInfoModel = it)
    }.scopedSubscribe()
  }

  fun handleBuyClick(adyenCard: AdyenCardWrapper, shouldStoreCard: Boolean, returnUrl: String) {
    sendSideEffect { ManageAdyenPaymentSideEffect.ShowLoading }
    /*transactionOriginUseCase(args.transactionBuilder)
      .flatMap { origin ->
//        events.startTimingForPurchaseEvent()
//        events.sendPaymentConfirmationEvent(args.transactionBuilder, args.paymentType)
        adyenPaymentInteractor.makePayment(
          adyenPaymentMethod = adyenCard.cardPaymentMethod,
          shouldStoreMethod = shouldStoreCard,
          hasCvc = adyenCard.hasCvc,
          supportedShopperInteraction = adyenCard.supportedShopperInteractions,
          returnUrl = returnUrl,
          value = args.amount,
          currency = args.currency,
          reference = args.transactionBuilder.orderReference,
          paymentType =//args.paymentType.mapToService().transactionType // TODO add transactionType (INAPP_UNMANAGED),
          origin = origin,
          packageName = args.transactionBuilder.domain,
          metadata = args.transactionBuilder.payload,
          sku = args.transactionBuilder.skuId,
          callbackUrl = args.transactionBuilder.callbackUrl,
          transactionType = args.transactionBuilder.type,
          referrerUrl = args.transactionBuilder.referrerUrl
        )
      }
      .doOnSuccess { paymentModel ->
        handlePaymentResult(paymentModel)
      }.scopedSubscribe()*/
  }

  private fun handlePaymentResult(paymentModel: PaymentModel) {
    when {
      paymentModel.refusalReason != null -> {
        paymentModel.refusalCode?.let { code ->
          when (code) {
            AdyenErrorCodeMapper.CVC_DECLINED -> sendSideEffect { ManageAdyenPaymentSideEffect.ShowCvvError }
            else -> sendSideEffect {
              ManageAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
            }
          }
        }
//        events.sendPaymentErrorEvent(
//          args.transactionBuilder,
//          args.paymentType,
//          paymentModel.refusalCode,
//          paymentModel.refusalReason
//        )
      }

      paymentModel.status == PaymentModel.Status.PENDING_USER_PAYMENT && paymentModel.action != null -> {
        handleAdyenAction(paymentModel)
      }

      else -> sendSideEffect {
        ManageAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel)
      }
    }
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      when (val type = paymentModel.action?.type) {
        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
//          events.send3dsStart(type)
          cachedUid = paymentModel.uid
          sendSideEffect { ManageAdyenPaymentSideEffect.Handle3DS(paymentModel.action) }
        }

        else -> {
          sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel) }
        }
      }
    }
  }

  fun handleBackButton() {
//    events.sendPaymentMethodEvent(
//      args.transactionBuilder,
//      args.paymentType,
//      BillingAnalytics.ACTION_BACK
//    )
    sendSideEffect { ManageAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
  }

  private fun handlePaypalResult(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentResult(paymentModel) }
    }
  }

  fun handleRedirectComponentResponse(actionComponentData: ActionComponentData) {
    adyenPaymentInteractor.submitRedirect(
      uid = cachedUid,
      details = convertToJson(actionComponentData.details!!),
      paymentData = actionComponentData.paymentData
    )
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { paymentModel ->
        handlePaymentResult(paymentModel)
      }.scopedSubscribe()
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
//      events.send3dsCancel()
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
    } else {
//      events.send3dsError(componentError.errorMessage)
    }
  }
}