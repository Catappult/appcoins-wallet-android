package com.asfoundation.wallet.transfers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.main.nav_bar.CurrencyNavigationItem
import com.asfoundation.wallet.main.nav_bar.TransferNavigationItem
import com.asfoundation.wallet.ui.bottom_navigation.CurrencyDestinations
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TransferFundsViewModel @Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  val clickedTransferItem: MutableState<Int> = mutableStateOf(TransferDestinations.SEND.ordinal)
  val clickedCurrencyItem: MutableState<Int> = mutableStateOf(CurrencyDestinations.APPC_C.ordinal)

  init {
    getWalletInfo()
  }

  private fun getWalletInfo() {
    observeWalletInfoUseCase(null, update = true, updateFiat = true)
      .firstOrError()
      .doOnSuccess { _uiState.value = UiState.Success(it) }
      .subscribe()
  }

  fun displayChat() = displayChatUseCase()

  fun transferNavigationItems() = listOf(
    TransferNavigationItem(
      destination = TransferDestinations.SEND,
      label = R.string.p2p_send_title,
      selected = true
    ),
    TransferNavigationItem(
      destination = TransferDestinations.RECEIVE,
      label = R.string.title_my_address,
      selected = false
    )
  )

  fun currencyNavigationItems() = listOf(
    CurrencyNavigationItem(
      destination = CurrencyDestinations.APPC_C,
      label = R.string.appc_credits_token_name,
      selected = true
    ),
    CurrencyNavigationItem(
      destination = CurrencyDestinations.APPC,
      label = R.string.appc_token_name,
      selected = false
    ),
    CurrencyNavigationItem(
      destination = CurrencyDestinations.ETHEREUM,
      label = R.string.ethereum_token_name,
      selected = false
    )
  )

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val walletInfo: WalletInfo) : UiState()
  }
}