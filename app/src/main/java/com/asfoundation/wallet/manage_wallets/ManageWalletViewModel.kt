package com.asfoundation.wallet.manage_wallets

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.ui.wallets.activeWalletAddress
import com.asfoundation.wallet.ui.wallets.inactiveWallets
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

  val openBottomSheet = mutableStateOf(false)
  val inactiveWalletBalance = mutableStateOf(WalletBalance())

  fun displayChat() {
    displayChatUseCase()
  }

  init {
    getWallets()
  }

  private fun getWallets(walletChanged: Boolean = false) {
    walletsInteract.observeWalletsModel()
      .firstOrError()
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { wallets ->
        getActiveWallet(wallets)
        if (walletChanged) _uiState.value = UiState.WalletChanged
      }
      .subscribe()
  }

  private fun getActiveWallet(wallets: WalletsModel) {
    observeWalletInfoUseCase(
      wallets.activeWalletAddress(),
      update = true,
      updateFiat = true
    )
      .firstOrError()
      .doOnSuccess {
        _uiState.value = UiState.Success(it, wallets.inactiveWallets())
      }.subscribe()
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object WalletChanged : UiState()
    data class Success(
      val activeWalletInfo: WalletInfo,
      val inactiveWallets: List<WalletBalance>
    ) : UiState()
  }

  fun changeActiveWallet(wallet: String) {
    walletDetailsInteractor.setActiveWallet(wallet)
      .doOnComplete { getWallets(walletChanged = true) }
      .subscribe()
  }
}
