package com.asfoundation.wallet.manage_wallets

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
      .just(Unit)
      .flatMap {
        observeWalletInfoUseCase(null, update = true, updateFiat = true)
          .map { walletInfo ->
            _uiState.value = UiState.Balance(walletInfo.walletBalance)
          }
      }
      .subscribe()

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Balance(val balance: WalletBalance) : UiState()
  }
}
