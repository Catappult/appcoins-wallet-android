package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.use_case.GetCountryCodeUseCase
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.emptyBonusInfoData
import com.asfoundation.wallet.iab.presentation.toPaymentMethodData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel(
  private val getCountryCodeUseCase: GetCountryCodeUseCase,
  private val paymentManager: PaymentManager,
  private val purchaseData: PurchaseData,
) : ViewModel() {

  private val viewModelState =
    MutableStateFlow<MainFragmentUiState>(MainFragmentUiState.LoadingDisclaimer)

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  init {
    paymentManager.selectedPaymentMethod
      .onEach { reload(false) }
      .launchIn(viewModelScope)

    reload()
  }

  fun reload(showLoading: Boolean = true) {
    viewModelScope.launch {
      if (showLoading) {
        viewModelState.update { MainFragmentUiState.LoadingDisclaimer }
      }

      try {
        val hasPreselectedPaymentMethod = paymentManager.hasPreSelectedPaymentMethod()
        val selectedPaymentMethod =
          if (hasPreselectedPaymentMethod) paymentManager.getSelectedPaymentMethod() else null

        val networkResponse = getCountryCodeUseCase()

        val showDisclaimer = networkResponse.showRefundDisclaimer == 1

        if (showLoading) {
          viewModelState.update {
            MainFragmentUiState.LoadingPurchaseData(
              showDisclaimer = showDisclaimer,
              showPreSelectedPaymentMethod = hasPreselectedPaymentMethod
            )
          }
        }

        val productInfoData = paymentManager.getProductInfo(
          packageName = purchaseData.domain,
          skuId = purchaseData.skuId ?: ""
        ) ?: throw RuntimeException("SkuId ${purchaseData.skuId} not found")

        viewModelState.update {
          MainFragmentUiState.Idle(
            showDisclaimer = showDisclaimer,
            preSelectedPaymentMethod = selectedPaymentMethod?.toPaymentMethodData(),
            bonusAvailable = true, // TODO check if bonus is available for the product,
            purchaseInfoData = PurchaseInfoData(
              packageName = purchaseData.domain,
              cost = "${productInfoData.transaction.currencySymbol} ${productInfoData.transaction.amount}",
              productName = productInfoData.title,
              hasFees = selectedPaymentMethod?.paymentMethod?.fee != null
            ),
            bonusInfoData = emptyBonusInfoData,
            purchaseData = purchaseData
          )
        }

      } catch (error: Throwable) {
        error.printStackTrace()
        viewModelState.update { MainFragmentUiState.Error }
      }
    }
  }
}

@Composable
fun rememberMainViewModel(
  purchaseData: PurchaseData,
  paymentManager: PaymentManager,
): MainViewModel {
  val injectionsProvider = hiltViewModel<MainViewModelInjectionsProvider>()
  return viewModel<MainViewModel>(
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
          paymentManager = paymentManager,
          purchaseData = purchaseData,
          getCountryCodeUseCase = injectionsProvider.getCountryCodeUseCase
        ) as T
      }
    }
  )
}

@HiltViewModel
private class MainViewModelInjectionsProvider @Inject constructor(
  val getCountryCodeUseCase: GetCountryCodeUseCase,
) : ViewModel()
