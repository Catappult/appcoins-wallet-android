package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.nfts.domain.NFTItem

class NFTDetailsViewModelFactory(val data: NFTItem) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return NFTDetailsViewModel(data) as T
  }
}