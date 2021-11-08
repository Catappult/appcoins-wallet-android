package com.asfoundation.wallet.nfts.ui.nftdetails

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.nfts.domain.NFTItem

sealed class NFTDetailsSideEffect : SideEffect

data class NFTDetailsState(val data: NFTItem) : ViewState

class NFTDetailsViewModel(private val data: NFTItem) :
    BaseViewModel<NFTDetailsState, NFTDetailsSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: NFTItem): NFTDetailsState {
      return NFTDetailsState(data)
    }
  }
}