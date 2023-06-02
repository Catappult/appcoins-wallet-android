package com.asfoundation.wallet.manage_wallets

import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
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
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val walletsInteract: WalletsInteract,
  private val walletDetailsInteractor: WalletDetailsInteractor,
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    displayChatUseCase()
  }

  init {
    getWallets()
  }

  private fun getWallets() {
    Observable.combineLatest(
      observeWalletInfoUseCase(null, update = true, updateFiat = true),
      walletsInteract.observeWalletsModel()
    ) { activeWalletInfo, inactiveWallets ->
      UiState.Success(activeWalletInfo, inactiveWallets.wallets.filter { !it.isActiveWallet })
    }
      .doOnSubscribe {
        _uiState.value = UiState.Loading
      }
      .doOnNext { newState ->
        _uiState.value = newState
      }.subscribe()
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val activeWalletInfo: WalletInfo, val inactiveWallets: List<WalletBalance>) :
      UiState()
  }

  fun changeActiveWallet(wallet: String) {
    walletDetailsInteractor.setActiveWallet(wallet)
      .doOnComplete { TODO() }
      .subscribe()
  }
}
