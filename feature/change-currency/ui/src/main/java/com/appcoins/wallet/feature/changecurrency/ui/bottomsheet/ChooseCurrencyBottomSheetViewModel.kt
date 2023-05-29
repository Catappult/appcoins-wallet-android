package com.appcoins.wallet.feature.changecurrency.ui.bottomsheet

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.changecurrency.data.use_cases.SetSelectedCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

object ChooseCurrencyBottomSheetSideEffect : SideEffect

data class ChooseCurrencyBottomSheetState(
  val selectedConfirmationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChooseCurrencyBottomSheetViewModel @Inject constructor(
  private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase,
) : NewBaseViewModel<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSheetSideEffect>(
  ChooseCurrencyBottomSheetState()
) {

  fun currencyConfirmationClick(chosenCurrency: String) {
    viewModelScope.launch {
      suspend { setSelectedCurrencyUseCase(chosenCurrency) }
        .mapSuspendToAsync() {
          copy(selectedConfirmationAsync = it)
        }
    }
  }
}