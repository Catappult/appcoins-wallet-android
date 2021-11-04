package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase

class NFTViewModelFactory(private val getNFTListUseCase: GetNFTListUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NFTViewModel(getNFTListUseCase) as T
    }
}