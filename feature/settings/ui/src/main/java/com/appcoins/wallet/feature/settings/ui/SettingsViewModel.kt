package com.appcoins.wallet.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  //private val settingsInteractor: SettingsInteractor,
  //private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
  //private val displayChatUseCase: DisplayChatUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    //displayChatUseCase()
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
  }
}