package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.ui.common.StringProvider
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.iab.di.AdyenReturnUrl
import com.asfoundation.wallet.iab.domain.use_case.SubmitRedirectUseCase
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.credit_card.error.AdyenErrorHandler
import com.asfoundation.wallet.iab.payment_manager.payment_methods.credit_card.error.AdyenErrorHandlerImpl
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class CreditCardViewModel(
  private val stringProvider: StringProvider,
  private val submitRedirectUseCase: SubmitRedirectUseCase,
  private val paymentManager: PaymentManager,
  private val redirectComponent: RedirectComponent,
  private val adyen3DS2Component: Adyen3DS2Component,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val cardConfiguration: CardConfiguration,
  private val paymentMethodId: String,
  private val returnUrl: String,
  private val adyenErrorHandler: AdyenErrorHandler,
) : ViewModel() {
  private companion object {
    const val REDIRECT = "redirect"
    const val THREEDS2 = "threeDS2"
    const val THREEDS2FINGERPRINT = "threeDS2Fingerprint"
    const val THREEDS2CHALLENGE = "threeDS2Challenge"
  }

  private val paymentMethod by lazy {
    paymentManager.getPaymentMethodById(paymentMethodId) as CreditCardPaymentMethod
  }

  private val viewModelState =
    MutableStateFlow<CreditCardUiState>(CreditCardUiState.Loading)

  private val viewModelStateSavingCard =
    MutableStateFlow(false)

  private val viewModelStateInputError =
    MutableStateFlow<String?>(null)

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  val uiStateSavingCard = viewModelStateSavingCard
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelStateSavingCard.value
    )

  val uiStateInputError = viewModelStateInputError
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelStateInputError.value
    )

  init {
    loadCreditCard()
  }

  fun loadCreditCard() {
    viewModelScope.launch {
      viewModelStateSavingCard.update { false }
      viewModelStateInputError.update { null }
      viewModelState.update { CreditCardUiState.Loading }

      try {
        val purchaseData = paymentManager.purchaseData
        val productInfoData = paymentManager.getProductInfo()
          ?: throw RuntimeException("SkuId ${purchaseData.skuId} not found")

        val paymentInfoModel = paymentMethod.loadPaymentInfo()

        viewModelState.update {
          CreditCardUiState.Idle(
            paymentInfoModel = paymentInfoModel,
            cardComponent = { activity ->
              paymentInfoModel.cardComponent?.invoke(
                activity,
                cardConfiguration
              )
            },
            creditCardPaymentMethod = paymentMethod,
            savingCreditCard = false,
            purchaseInfoData = PurchaseInfoData(
              packageName = purchaseData.domain,
              cost = paymentMethod.run {
                currencyFormatUtils.formatCost(
                  currencySymbol = currencySymbol,
                  currencyCode = currency,
                  cost = cost
                )
              },
              productName = productInfoData.title,
              hasFees = paymentMethod.hasFees,
              fees = paymentMethod.run {
                currencyFormatUtils.formatCost(
                  currencySymbol = currencySymbol,
                  currencyCode = currency,
                  cost = fees
                )
              }.takeIf { paymentMethod.hasFees },
              subtotal = paymentMethod.run {
                currencyFormatUtils.formatCost(
                  currencySymbol = currencySymbol,
                  currencyCode = currency,
                  cost = subtotal
                )
              }.takeIf { paymentMethod.hasFees }
            ),
          )
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        viewModelState.update { CreditCardUiState.CreditCardError }
      }
    }
  }

  fun saveCreditCardData(adyenCardWrapper: AdyenCardWrapper?) {
    viewModelScope.launch {
      viewModelStateSavingCard.update { true }
      viewModelStateInputError.update { null }

      try {
        val paymentModel = paymentMethod.addCard(adyenCardWrapper!!, returnUrl)

        handlePaymentModelResult(paymentModel)
      } catch (e: Throwable) {
        e.printStackTrace()
        viewModelState.update { CreditCardUiState.AddCreditCardError(stringProvider.getString(R.string.unknown_error)) }
        viewModelStateSavingCard.update { false }
      }
    }
  }

  fun onWebViewResult(actionResolution: ActionResolution) {
    viewModelScope.launch {
      when (actionResolution) {
        ActionResolution.Cancel -> {
          viewModelStateSavingCard.update { false }
          viewModelStateInputError.update { null }
        }

        ActionResolution.Fail -> {
          viewModelStateSavingCard.update { false }
          viewModelStateInputError.update { null }
          viewModelState.update { CreditCardUiState.AddCreditCardError(stringProvider.getString(R.string.unknown_error)) }
        }

        is ActionResolution.Success -> {
          viewModelState.update {
            CreditCardUiState.HandleRedirectAction(
              component = redirectComponent,
              uri = actionResolution.data,
              paymentModel = actionResolution.paymentModel
            )
          }
        }
      }
    }
  }

  fun submitActionResult(paymentData: String?, details: JSONObject?, paymentModel: PaymentModel) {
    viewModelScope.launch {
      viewModelState.update { CreditCardUiState.Loading }

      try {
        val result = submitRedirectUseCase(
          uid = paymentModel.uid,
          details = details,
          paymentData = paymentData ?: paymentModel.paymentData
        )

        handlePaymentModelResult(result)
      } catch (e: Throwable) {
        e.printStackTrace()
        viewModelState.update { CreditCardUiState.CreditCardError }
      }
    }
  }

  private suspend fun handlePaymentModelResult(paymentModel: PaymentModel) {
    if (paymentModel.status == PaymentModel.Status.COMPLETED) {
      paymentManager.setSelectedPaymentMethod(paymentMethod)

      viewModelState.update { CreditCardUiState.Finish }
    } else if (paymentModel.status == PaymentModel.Status.PENDING_USER_PAYMENT && paymentModel.action?.type != null) {
      val state = when (paymentModel.action?.type) {
        REDIRECT ->
          CreditCardUiState.UserAction(redirectComponent, paymentModel)

        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE ->
          CreditCardUiState.Handle3DSAction(adyen3DS2Component, paymentModel)

        else ->
          CreditCardUiState.AddCreditCardError(stringProvider.getString(R.string.unknown_error))
      }

      viewModelState.update { state }

    } else {
      viewModelStateSavingCard.update { false }

      when {
        adyenErrorHandler.isCVCError(paymentModel) ->
          viewModelStateInputError.update { stringProvider.getString(R.string.purchase_card_error_CVV) }

        adyenErrorHandler.isCVCRequired(paymentModel) ->
          viewModelStateInputError.update { "CVC Required" } // TODO hardcoded string

        adyenErrorHandler.shouldShowVerifyScreen(paymentModel) -> {
          val message = adyenErrorHandler.handleError(paymentModel)
          viewModelState.update { CreditCardUiState.VerifyScreen(message) }
        }

        else -> {
          val message = adyenErrorHandler.handleError(paymentModel)
          viewModelState.update { CreditCardUiState.AddCreditCardError(message) }
        }
      }
    }
  }

  @SuppressLint("StaticFieldLeak")
  fun clearAdyenComponents(activity: ComponentActivity) {
    ViewModelProvider(activity).run {
      listOf(
        getAdyenComponentVMKey<CardComponent>(),
        getAdyenComponentStoredVMKey<CardComponent>(),
      ).forEach {
        try {
          //Invalidates current Adyen ViewModel, since the provided modelClass is not Adyen component
          get(key = it, modelClass = (object : ViewModel() {})::class.java)
          activity.savedStateRegistry.unregisterSavedStateProvider(it)
        } catch (e: Throwable) {
          e.printStackTrace()
        }
      }
    }
  }

  private inline fun <reified T : Any> getAdyenComponentVMKey() =
    "androidx.lifecycle.ViewModelProvider.DefaultKey:${T::class.java.canonicalName}"

  private inline fun <reified T : Any> getAdyenComponentStoredVMKey() =
    "androidx.lifecycle.ViewModelProvider.DefaultKey:${T::class.java.canonicalName}/stored"

}

@Composable
fun rememberCreditCardViewModel(
  activity: ComponentActivity,
  paymentManager: PaymentManager,
  paymentMethodId: String,
): CreditCardViewModel {
  val injectionsProvider = hiltViewModel<CreditCardModelInjectionsProvider>()
  val viewModel = viewModel<CreditCardViewModel>(
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreditCardViewModel(
          stringProvider = injectionsProvider.stringProvider,
          paymentManager = paymentManager,
          redirectComponent = injectionsProvider.redirectComponentProvider(activity),
          adyen3DS2Component = injectionsProvider.adyen3DS2ComponentProvider(activity),
          currencyFormatUtils = injectionsProvider.currencyFormatUtils,
          cardConfiguration = injectionsProvider.cardConfiguration,
          paymentMethodId = paymentMethodId,
          returnUrl = injectionsProvider.returnUrl,
          adyenErrorHandler = AdyenErrorHandlerImpl(
            stringProvider = injectionsProvider.stringProvider,
            paymentType = paymentMethodId,
            adyenPaymentInteractor = injectionsProvider.adyenPaymentInteractor,
            adyenErrorCodeMapper = injectionsProvider.adyenErrorCodeMapper,
            servicesErrorCodeMapper = injectionsProvider.servicesErrorCodeMapper,
          ),
          submitRedirectUseCase = injectionsProvider.submitRedirectUseCase,
        ) as T
      }
    }
  )
  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearAdyenComponents(activity)
    }
  }
  return viewModel
}

@HiltViewModel
private class CreditCardModelInjectionsProvider @Inject constructor(
  val stringProvider: StringProvider,
  val submitRedirectUseCase: SubmitRedirectUseCase,
  val redirectComponentProvider: RedirectComponentProvider,
  val adyen3DS2ComponentProvider: Adyen3DS2ComponentProvider,
  val currencyFormatUtils: CurrencyFormatUtils,
  val cardConfiguration: CardConfiguration,
  val adyenPaymentInteractor: AdyenPaymentInteractor,
  val adyenErrorCodeMapper: AdyenErrorCodeMapper,
  val servicesErrorCodeMapper: ServicesErrorCodeMapper,
  @AdyenReturnUrl val returnUrl: String,
) : ViewModel()

fun interface RedirectComponentProvider {
  operator fun invoke(activity: ComponentActivity): RedirectComponent
}

fun interface Adyen3DS2ComponentProvider {
  operator fun invoke(activity: ComponentActivity): Adyen3DS2Component
}
