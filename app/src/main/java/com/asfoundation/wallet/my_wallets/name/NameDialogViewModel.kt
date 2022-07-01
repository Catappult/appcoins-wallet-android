package com.asfoundation.wallet.my_wallets.name

import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class NameDialogSideEffect : SideEffect {
  object NavigateBack : NameDialogSideEffect()
}

data class NameDialogState(
  val walletAddress: String,
  val walletNameAsync: Async<String> = Async.Uninitialized
) : ViewState

@HiltViewModel
class NameDialogViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val walletInfoRepository: WalletInfoRepository,
) :
  BaseViewModel<NameDialogState, NameDialogSideEffect>(initialState(savedStateHandle)) {

  companion object {
    fun initialState(savedStateHandle: SavedStateHandle): NameDialogState = NameDialogState(
      savedStateHandle.get<String>(NameDialogFragment.WALLET_ADDRESS_KEY)!!,
      Async.Success(savedStateHandle.get<String>(NameDialogFragment.WALLET_NAME_KEY)!!),
    )
  }

  fun setName(name: String) {
    val retainValue = NameDialogState::walletNameAsync
    state.walletNameAsync()?.also {
      walletInfoRepository.updateWalletName(state.walletAddress, name)
        .toSingleDefault(it)
        .delay(500, TimeUnit.MILLISECONDS) // for progress showing
        .asAsyncToState(retainValue) { walletName -> copy(walletNameAsync = walletName) }
        .doOnSuccess { sendSideEffect { NameDialogSideEffect.NavigateBack } }
        .scopedSubscribe(Throwable::printStackTrace)
    }
  }
}