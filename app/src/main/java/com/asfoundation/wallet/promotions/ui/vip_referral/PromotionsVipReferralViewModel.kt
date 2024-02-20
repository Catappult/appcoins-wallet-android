package com.asfoundation.wallet.promotions.ui.vip_referral

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.promotions.usecases.ConvertToLocalFiatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PromotionsVipReferralViewModel
@Inject
constructor(
    private val convertToLocalFiatUseCase: ConvertToLocalFiatUseCase,
    private val displayChatUseCase: DisplayChatUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun getCurrency(earnedValue: String) {
    convertToLocalFiatUseCase(earnedValue, "USD")
        .doOnSuccess { _uiState.value = UiState.Success(it) }
        .doOnError { _uiState.value = UiState.Fail(it) }
        .doOnSubscribe { _uiState.value = UiState.Loading }
        .subscribe()
  }

  fun displayChat() = displayChatUseCase()

  sealed class UiState {
    object Idle : UiState()

    object Loading : UiState()

    data class Fail(val error: Throwable) : UiState()

    data class Success(val fiatValue: FiatValue?) : UiState()
  }
}
