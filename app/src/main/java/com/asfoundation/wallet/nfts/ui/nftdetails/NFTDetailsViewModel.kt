package com.asfoundation.wallet.nfts.ui.nftdetails

import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.nfts.domain.NFTItem

sealed class NFTDetailsSideEffect : com.appcoins.wallet.ui.arch.SideEffect

data class NFTDetailsState(val data: NFTItem) : com.appcoins.wallet.ui.arch.ViewState

class NFTDetailsViewModel(private val data: NFTItem) :
    com.appcoins.wallet.ui.arch.BaseViewModel<NFTDetailsState, NFTDetailsSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: NFTItem): NFTDetailsState {
      return NFTDetailsState(data)
    }
  }
}