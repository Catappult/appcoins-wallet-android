package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.usecases.EstimateNFTSendGasUseCase
import com.asfoundation.wallet.nfts.usecases.SendNFTUseCase

class NFTTransactDialogViewModelFactory(val data: NFTItem,
                                        private val estimateNFTSendGasUseCase: EstimateNFTSendGasUseCase,
                                        private val sendNFTUseCase: SendNFTUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return NFTTransactDialogViewModel(data, estimateNFTSendGasUseCase, sendNFTUseCase) as T
  }
}