package com.asfoundation.wallet.manage_wallets

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManageWalletViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    displayChatUseCase()
  }

  init {
    observeBalance()
  }

  private fun observeBalance() =
    Observable
      .interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
      .flatMap {
        observeWalletInfoUseCase(null, update = true, updateFiat = true)
          .map { walletInfo ->
            _uiState.value = UiState.Success(walletInfo)
          }
      }
      .subscribe()

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val walletInfo: WalletInfo) : UiState()
  }

  companion object {
    const val UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS
  }
}
