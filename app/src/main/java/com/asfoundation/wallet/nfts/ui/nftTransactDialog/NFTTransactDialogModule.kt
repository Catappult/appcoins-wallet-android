package com.asfoundation.wallet.nfts.ui.nftTransactDialog

import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.usecases.EstimateNFTSendGasUseCase
import com.asfoundation.wallet.nfts.usecases.SendNFTUseCase
import dagger.Module
import dagger.Provides

@Module
class NFTTransactDialogModule {

  @Provides
  fun provideNFTTransactDialogViewModelFactory(data: NFTItem,
                                               estimateNFTSendGasUseCase: EstimateNFTSendGasUseCase,
                                               sendNFTUseCase: SendNFTUseCase): NFTTransactDialogViewModelFactory {
    return NFTTransactDialogViewModelFactory(data, estimateNFTSendGasUseCase, sendNFTUseCase)
  }

  @Provides
  fun provideNFTDetailsData(fragment: NFTTransactDialogFragment): NFTItem {
    fragment.requireArguments()
        .apply {
          return getSerializable(NFTTransactDialogFragment.NFT_ITEM_DATA)!! as NFTItem
        }
  }

}