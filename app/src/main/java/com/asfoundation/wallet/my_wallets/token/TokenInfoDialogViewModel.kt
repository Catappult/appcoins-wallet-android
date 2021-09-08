package com.asfoundation.wallet.my_wallets.token

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState

object TokenInfoSideEffect : SideEffect

data class TokenInfoState(
    val title: String,
    val image: String,
    val description: String,
    val showTopUp: Boolean
) : ViewState

class TokenInfoDialogViewModel(
    private val data: TokenInfoDialogData
) : BaseViewModel<TokenInfoState, TokenInfoSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: TokenInfoDialogData): TokenInfoState {
      return TokenInfoState(data.title, data.image, data.description, data.showTopUp)
    }
  }
}
