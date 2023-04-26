package com.appcoins.wallet.feature.changecurrency.ui.bottomsheet

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.appcoins.wallet.feature.changecurrency.data.use_cases.SetSelectedCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

sealed class ChooseCurrencyBottomSideEffect : SideEffect {
  object NavigateBack : ChooseCurrencyBottomSideEffect()
}

data class ChooseCurrencyBottomSheetState(
  val selectedConfirmationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChooseCurrencyBottomSheetViewModel @Inject constructor(
  private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase,
  private val dispatchers: Dispatchers,
) : BaseViewModel<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect>(
  ChooseCurrencyBottomSheetState()
) {

  fun currencyConfirmationClick(chosenCurrency: String) {
    rxSingle(dispatchers.io) { setSelectedCurrencyUseCase(chosenCurrency) }
      .asAsyncToState() {
        copy(selectedConfirmationAsync = it)
      }
      .scopedSubscribe { e ->
        e.printStackTrace()
      }
  }
}