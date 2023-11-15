package com.appcoins.wallet.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.BuildUpdateIntentUseCase
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import com.github.michaelbull.result.get
import com.wallet.appcoins.feature.support.data.DisplayChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  private val disposables: CompositeDisposable,
  //private val settingsInteractor: SettingsInteractor,
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  private val _uiCurrencyState = MutableStateFlow<UiCurrencyState>(UiCurrencyState.Loading)
  var uiCurrencyState: StateFlow<UiCurrencyState> = _uiCurrencyState

  fun buildIntentToStore() = buildUpdateIntentUseCase()

  init {
    fetchCurrency()
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    _uiState.value = UiState.Error
  }

  private fun fetchCurrency() {
    disposables.add(rxSingle { getChangeFiatCurrencyModelUseCase() }
      .observeOn(rxSchedulers.main)
      .doOnSuccess { result ->
        result.get()?.let {
          for (fiatCurrency in it.list) {
            if (fiatCurrency.currency == it.selectedCurrency) {
              _uiCurrencyState.value = UiCurrencyState.Success(fiatCurrency)
              break
            }
          }
        }
      }
      .subscribeOn(rxSchedulers.io)
      .subscribe())
  }

  fun displayChat() = displayChatUseCase()

  fun navigateToChangeCurrency() {
    TODO("Not yet implemented")
  }

  sealed class UiState {
    object Error : UiState()
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
  }

  sealed class UiCurrencyState {
    object Error : UiCurrencyState()
    object Loading : UiCurrencyState()
    data class Success(val currency: FiatCurrency) : UiCurrencyState()
  }
}