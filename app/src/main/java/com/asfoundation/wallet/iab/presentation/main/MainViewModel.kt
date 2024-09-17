package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.emptyBonusInfoData
import com.asfoundation.wallet.iab.presentation.emptyPaymentMethodData
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.iab.FragmentNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel(
  private val fragmentNavigator: FragmentNavigator,
  private val countryCodeProvider: CountryCodeRepository,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
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
    reload()
  }

  fun reload() {
    viewModelScope.launch {
      viewModelState.update { MainFragmentUiState.LoadingDisclaimer }

      try {
        val hasPreselectedPaymentMethod = inAppPurchaseInteractor.hasPreSelectedPaymentMethod()
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
            showPreSelectedPaymentMethod = hasPreselectedPaymentMethod,
            preSelectedPaymentMethodEnabled = true, // TODO pre selected method might not be available for certain products
            bonusAvailable = true, // TODO check if bonus is available for the product,
            purchaseInfoData = emptyPurchaseInfo.copy(packageName = purchaseData.domain),
            bonusInfoData = emptyBonusInfoData,
            paymentMethodData = emptyPaymentMethodData,
          )
        }

      } catch (error: Throwable) {
        error.printStackTrace()
        viewModelState.update { MainFragmentUiState.Error }
      }
    }
  }

  fun navigateTo(directions: NavDirections) {
    fragmentNavigator.navigateTo(directions)
  }
}

@Composable
fun rememberMainViewModel(
  navController: NavController,
  purchaseData: PurchaseData
): MainViewModel {
  val injectionsProvider = hiltViewModel<MainViewModelInjectionsProvider>()
  return viewModel<MainViewModel>(
    key = navController.hashCode().toString(),
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
          fragmentNavigator = FragmentNavigator(navController),
          countryCodeProvider = injectionsProvider.countryCodeProvider,
          networkDispatcher = injectionsProvider.networkDispatcher,
          inAppPurchaseInteractor = injectionsProvider.inAppPurchaseInteractor,
          purchaseData = purchaseData
        ) as T
      }
    }
  )
}

@HiltViewModel
private class MainViewModelInjectionsProvider @Inject constructor(
  val countryCodeProvider: CountryCodeRepository,
  val inAppPurchaseInteractor: InAppPurchaseInteractor,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
) : ViewModel()
