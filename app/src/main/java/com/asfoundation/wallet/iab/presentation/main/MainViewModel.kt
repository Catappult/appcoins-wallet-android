package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.presentation.emptyBonusInfoData
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.iab.presentation.toPaymentMethodData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel(
  private val countryCodeProvider: CountryCodeRepository,
  private val paymentManager: PaymentManager,
  private val networkDispatcher: CoroutineDispatcher,
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
      .onEach { paymentMethod ->
        viewModelState.getAndUpdate {
          when (it) {
            is MainFragmentUiState.LoadingPurchaseData -> it.copy(showPreSelectedPaymentMethod = paymentMethod != null)
            is MainFragmentUiState.Idle -> it.copy(preSelectedPaymentMethod = paymentMethod?.toPaymentMethodData())
            else -> it
          }
        }
      }
      .launchIn(viewModelScope)

    reload()
  }

  fun reload() {
    viewModelScope.launch {
      viewModelState.update { MainFragmentUiState.LoadingDisclaimer }

      try {
        val hasPreselectedPaymentMethod = paymentManager.hasPreSelectedPaymentMethod()
        val selectedPaymentMethod = if (hasPreselectedPaymentMethod) paymentManager.getSelectedPaymentMethod() else null

        val networkResponse = countryCodeProvider.getCountryCode().callAsync(networkDispatcher)

        val showDisclaimer = networkResponse.showRefundDisclaimer == 1
        viewModelState.update {
          MainFragmentUiState.LoadingPurchaseData(
            showDisclaimer = showDisclaimer,
            showPreSelectedPaymentMethod = hasPreselectedPaymentMethod
          )
        }

        delay(TimeUnit.SECONDS.toMillis(2L)) // TODO replace in future by product details endpoint

        viewModelState.update {
          MainFragmentUiState.Idle(
            showDisclaimer = showDisclaimer,
            preSelectedPaymentMethod = selectedPaymentMethod?.toPaymentMethodData(),
            bonusAvailable = true, // TODO check if bonus is available for the product,
            purchaseInfoData = emptyPurchaseInfo.copy(packageName = purchaseData.domain),
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
          countryCodeProvider = injectionsProvider.countryCodeProvider,
          networkDispatcher = injectionsProvider.networkDispatcher,
          paymentManager = paymentManager,
          purchaseData = purchaseData
        ) as T
      }
    }
  )
}

@HiltViewModel
private class MainViewModelInjectionsProvider @Inject constructor(
  val countryCodeProvider: CountryCodeRepository,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
) : ViewModel()
