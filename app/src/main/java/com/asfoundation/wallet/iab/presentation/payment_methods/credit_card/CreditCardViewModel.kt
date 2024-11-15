package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.card.CardConfiguration
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreditCardViewModel(
  private val paymentManager: PaymentManager,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val cardConfiguration: CardConfiguration,
  private val paymentMethodId: String,
) : ViewModel() {

  private val paymentMethod by lazy {
    paymentManager.getPaymentMethodById(paymentMethodId) as CreditCardPaymentMethod
  }

  private val viewModelState =
    MutableStateFlow<CreditCardUiState>(CreditCardUiState.Loading)

  private val viewModelStateSavingCard =
    MutableStateFlow(false)

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

  init {
    loadCreditCard()
  }

  fun loadCreditCard() {
    viewModelScope.launch {
      viewModelState.update { CreditCardUiState.Loading }

      try {
        val purchaseData = paymentManager.purchaseData
        val productInfoData = paymentManager.getProductInfo()
          ?: throw RuntimeException("SkuId ${purchaseData.skuId} not found")

        val paymentInfoModel = paymentMethod.init()

        viewModelState.update {
          CreditCardUiState.Idle(
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
            )
          )
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        viewModelState.update { CreditCardUiState.PaymentMethodsError }
      }
    }
  }

  fun saveCreditCardData() {
    viewModelScope.launch {
      viewModelStateSavingCard.update { true }

      try {
        val cardSaved = paymentMethod.saveCard()

        if (cardSaved) {
          paymentManager.setSelectedPaymentMethod(paymentMethod)

          viewModelState.update { CreditCardUiState.Finish }

        } else {
          viewModelStateSavingCard.update { false }
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        viewModelStateSavingCard.update { false }
      }

    }
  }
}

@Composable
fun rememberCreditCardViewModel(
  paymentManager: PaymentManager,
  paymentMethodId: String,
): CreditCardViewModel {
  val injectionsProvider = hiltViewModel<CreditCardModelInjectionsProvider>()
  return viewModel<CreditCardViewModel>(
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreditCardViewModel(
          paymentManager = paymentManager,
          currencyFormatUtils = injectionsProvider.currencyFormatUtils,
          cardConfiguration = injectionsProvider.cardConfiguration,
          paymentMethodId = paymentMethodId
        ) as T
      }
    }
  )
}


@HiltViewModel
private class CreditCardModelInjectionsProvider @Inject constructor(
  val currencyFormatUtils: CurrencyFormatUtils,
  val cardConfiguration: CardConfiguration,
) : ViewModel()
