package com.asfoundation.wallet.manage_wallets

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.activeWalletAddress
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.inactiveWallets
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.interact.DeleteWalletInteract
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
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
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val deleteWalletInteract: DeleteWalletInteract
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  val openBottomSheet = mutableStateOf(false)
  val inactiveWalletBalance = mutableStateOf(WalletInfoSimple())

  fun displayChat() {
    displayChatUseCase()
  }

  init {
    getWallets()
  }

  fun updateWallets() = getWallets()


  private fun getWallets(walletChanged: Boolean = false) {
    walletsInteract
      .observeWalletsModel()
      .firstOrError()
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { wallets ->
        getActiveWallet(wallets)
        if (walletChanged) _uiState.value = UiState.WalletChanged
      }
      .subscribe()
  }

  private fun getActiveWallet(wallets: WalletsModel) {
    observeWalletInfoUseCase(wallets.activeWalletAddress(), update = true, updateFiat = true)
      .firstOrError()
      .doOnSuccess { _uiState.value = UiState.Success(it, wallets.inactiveWallets()) }
      .subscribe()
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object WalletChanged : UiState()
    object WalletDeleted : UiState()
    data class Success(
      val activeWalletInfo: WalletInfo,
      val inactiveWallets: List<WalletInfoSimple>
    ) : UiState()
  }

  fun changeActiveWallet(wallet: String) {
    walletDetailsInteractor
      .setActiveWallet(wallet)
      .doOnComplete { getWallets(walletChanged = true) }
      .subscribe()
  }

  fun setName(wallet: String, name: String) {
    updateWalletNameUseCase(wallet, name).doOnComplete { getWallets() }.subscribe()
  }

  fun deleteWallet(wallet: String) {
    deleteWalletInteract.delete(wallet)
      .doOnComplete {
        _uiState.value = UiState.WalletDeleted
      }
      .subscribe()
  }
}
