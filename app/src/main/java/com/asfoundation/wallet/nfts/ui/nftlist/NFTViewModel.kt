package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.navigation.fragment.FragmentNavigator
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTClick
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase

sealed class NFTSideEffect : SideEffect {
    data class NavigateToInfo(val nftData: NFTItem , val extras: FragmentNavigator.Extras) : NFTSideEffect()
}

data class NFTState(val nftListModelAsync: Async<List<NFTItem>> = Async.Uninitialized) :
    ViewState

class NFTViewModel(private val getNFTList: GetNFTListUseCase) :
    BaseViewModel<NFTState, NFTSideEffect>(initialState()) {

    companion object {
        fun initialState(): NFTState {
            return NFTState()
        }
    }

    fun fetchNFTList() {
        getNFTList()
            .asAsyncToState(NFTState::nftListModelAsync) {
                copy(nftListModelAsync = it)
            }
            .repeatableScopedSubscribe(NFTState::nftListModelAsync.name) { e ->
                e.printStackTrace()
            }
    }

    fun nftClicked(nftClick: NFTClick) {
        sendSideEffect {
            NFTSideEffect.NavigateToInfo(nftClick.data , nftClick.extras)
        }
    }
}