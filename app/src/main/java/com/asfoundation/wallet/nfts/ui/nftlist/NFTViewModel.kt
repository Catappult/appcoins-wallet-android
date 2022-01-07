package com.asfoundation.wallet.nfts.ui.nftlist

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTClick
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class NFTSideEffect : SideEffect {
  data class NavigateToInfo(val nftData: NFTItem) : NFTSideEffect()
}

data class NFTState(val nftListModelAsync: Async<List<NFTItem>> = Async.Uninitialized) : ViewState

@HiltViewModel
class NFTViewModel @Inject constructor(private val getNFTList: GetNFTListUseCase) :
    BaseViewModel<NFTState, NFTSideEffect>(initialState()) {

  companion object {
    fun initialState(): NFTState {
      return NFTState()
    }
  }

  fun fetchNFTList() {
    getNFTList().asAsyncToState(NFTState::nftListModelAsync) {
          copy(nftListModelAsync = it)
        }
        .repeatableScopedSubscribe(NFTState::nftListModelAsync.name) { e ->
          e.printStackTrace()
        }
  }

  fun nftClicked(nftClick: NFTClick) {
    sendSideEffect {
      NFTSideEffect.NavigateToInfo(nftClick.data)
    }
  }
}