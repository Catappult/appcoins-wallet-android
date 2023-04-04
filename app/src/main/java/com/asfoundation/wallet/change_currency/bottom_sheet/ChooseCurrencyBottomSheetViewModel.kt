package com.asfoundation.wallet.change_currency.bottom_sheet

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.backup.save_on_device.BackupSaveOnDeviceDialogFragment
import com.asfoundation.wallet.backup.save_on_device.BackupSaveOnDeviceDialogState
import com.asfoundation.wallet.backup.save_on_device.BackupSaveOnDeviceDialogViewModel
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Scheduler
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject


sealed class ChooseCurrencyBottomSideEffect : SideEffect {
  object NavigateBack : ChooseCurrencyBottomSideEffect()
}

data class ChooseCurrencyBottomSheetState(
  val selectedCurrency: String,
  val selectedFlag: String?,
  val selectedLabel: String,
  val selectedConfirmationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ChooseCurrencyBottomSheetViewModel @Inject constructor(
  private val setSelectedCurrencyUseCase: SetSelectedCurrencyUseCase,
  private val dispatchers: Dispatchers,
  savedStateHandle: SavedStateHandle
) : BaseViewModel<ChooseCurrencyBottomSheetState, ChooseCurrencyBottomSideEffect>(
  initialState(savedStateHandle)
) {

  companion object {
    fun initialState(savedStateHandle: SavedStateHandle) =
      savedStateHandle.run {
        val currency = get<String>(ChooseCurrencyBottomSheetFragment.CURRENCY)!!
        val flag = get<String>(ChooseCurrencyBottomSheetFragment.FLAG)!!
        val label = get<String>(ChooseCurrencyBottomSheetFragment.LABEL)!!
        ChooseCurrencyBottomSheetState(currency, flag, label)
      }
  }

  fun currencyConfirmationClick() {
    rxSingle(dispatchers.io) { setSelectedCurrencyUseCase(state.selectedCurrency) }
      .asAsyncToState() {
        copy(selectedConfirmationAsync = it)
      }
      .scopedSubscribe { e ->
        e.printStackTrace()
      }
  }
}