package com.asfoundation.wallet.manage_cards

import android.net.Uri
import androidx.activity.result.ActivityResult
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
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
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_cards.usecases.GetPaymentInfoNewCardModelUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import org.json.JSONObject
import javax.inject.Inject

sealed class ManageAdyenPaymentSideEffect : SideEffect {
  object NavigateToPaymentResult : ManageAdyenPaymentSideEffect()
  data class Handle3DS(val action: Action?) : ManageAdyenPaymentSideEffect()
  data class HandleRedirect(val url: String) : ManageAdyenPaymentSideEffect()
  data class HandleWebViewResult(val uri: Uri) : ManageAdyenPaymentSideEffect()
  object ShowLoading : ManageAdyenPaymentSideEffect()
  object ShowCvvError : ManageAdyenPaymentSideEffect()
  object NavigateToPaymentError : ManageAdyenPaymentSideEffect()
  object NavigateBackToPaymentMethods : ManageAdyenPaymentSideEffect()
}

data class ManageAdyenPaymentState(val paymentInfoModel: Async<PaymentInfoModel> = Async.Uninitialized) :
  ViewState

@HiltViewModel
class ManageAdyenPaymentViewModel @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val getPaymentInfoModelUseCase: GetPaymentInfoNewCardModelUseCase,
  private val rxSchedulers: RxSchedulers,
  private val displayChatUseCase: DisplayChatUseCase
) :
  BaseViewModel<ManageAdyenPaymentState, ManageAdyenPaymentSideEffect>(
    ManageAdyenPaymentState()
  ) {

  private lateinit var cachedUid: String
  private var cachedPaymentData: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  init {
    handlePaymentInfo()
  }

  private fun handlePaymentInfo() {
    getPaymentInfoModelUseCase(
      value = "0",
      currency = "EUR"
    ).asAsyncToState {
      copy(paymentInfoModel = it)
    }.scopedSubscribe()
  }

  fun handleBuyClick(adyenCard: AdyenCardWrapper, returnUrl: String) {
    sendSideEffect { ManageAdyenPaymentSideEffect.ShowLoading }
    adyenPaymentInteractor.addCard(
      adyenPaymentMethod = adyenCard.cardPaymentMethod,
      hasCvc = adyenCard.hasCvc,
      supportedShopperInteraction = adyenCard.supportedShopperInteractions,
      returnUrl = returnUrl,
      value = "0",
      currency = "EUR"
    )
      .flatMapCompletable { paymentModel ->
        handlePaymentResult(paymentModel)
      }.scopedSubscribe()
  }

  private fun handlePaymentResult(
    paymentModel: PaymentModel,
  ): Completable = when {
    paymentModel.resultCode.equals("AUTHORISED", true) -> {
      adyenPaymentInteractor.getAuthorisedTransaction(paymentModel.uid)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          when {
            it.status == PaymentModel.Status.COMPLETED -> Completable.fromAction {
              sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentResult }
            }

            isPaymentFailed(it.status) -> {
              Completable.fromAction {
                sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
              }
            }

            else -> {
              Completable.fromAction {
                sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
              }
            }
          }
        }
    }

    paymentModel.status == PaymentModel.Status.PENDING_USER_PAYMENT && paymentModel.action != null -> {
      Completable.fromAction {
        handleAdyenAction(paymentModel)
      }
    }

    paymentModel.refusalReason != null -> Completable.fromAction {
      var riskRules: String? = null
      paymentModel.refusalCode?.let { code ->
        when (code) {
          AdyenErrorCodeMapper.CVC_DECLINED -> sendSideEffect {
            ManageAdyenPaymentSideEffect.ShowCvvError
          }
          AdyenErrorCodeMapper.FRAUD -> {
            sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
          }

          else -> sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
        }
      }
    }

    paymentModel.error.hasError -> Completable.fromAction {
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
    }

    paymentModel.status == PaymentModel.Status.CANCELED -> Completable.fromAction {
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
    }

    else -> Completable.fromAction {
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
    }
  }

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == PaymentModel.Status.FAILED || status == PaymentModel.Status.CANCELED || status == PaymentModel.Status.INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      when (val type = paymentModel.action?.type) {
        AdyenResponseMapper.REDIRECT -> {
          cachedPaymentData = paymentModel.paymentData
          cachedUid = paymentModel.uid
          paymentModel.redirectUrl?.let {
            sendSideEffect { ManageAdyenPaymentSideEffect.HandleRedirect(it) }
          }
        }

        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
          cachedUid = paymentModel.uid
          sendSideEffect { ManageAdyenPaymentSideEffect.Handle3DS(paymentModel.action) }
        }

        else -> {
          sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
        }
      }
    }
  }


  fun handleRedirectComponentResponse(actionComponentData: ActionComponentData) {
    adyenPaymentInteractor.submitRedirect(
      uid = cachedUid,
      details = convertToJson(actionComponentData.details!!),
      paymentData = actionComponentData.paymentData
    )
      .subscribeOn(networkScheduler)
      .doOnError { sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError } }
      .flatMapCompletable { paymentModel ->
        handlePaymentResult(paymentModel)
      }
      .scopedSubscribe()
  }

  fun handleWebViewResult(result: ActivityResult) {
    when (result.resultCode) {
      WebViewActivity.FAIL -> {
        sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
      }

      WebViewActivity.SUCCESS -> {
        result.data!!.data?.let { uri ->
          sendSideEffect { ManageAdyenPaymentSideEffect.HandleWebViewResult(uri) }
        }
      }

      WebViewActivity.USER_CANCEL -> {
        sendSideEffect { ManageAdyenPaymentSideEffect.NavigateToPaymentError }
      }
    }
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
      sendSideEffect { ManageAdyenPaymentSideEffect.NavigateBackToPaymentMethods }
    } else {
    }
  }

  fun displayChat() {
    displayChatUseCase()
  }
}