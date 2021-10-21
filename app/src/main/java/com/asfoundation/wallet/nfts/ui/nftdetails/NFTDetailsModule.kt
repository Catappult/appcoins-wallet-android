package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.nfts.domain.NFTItem
import dagger.Module
import dagger.Provides

@Module
class NFTDetailsModule {
  @Provides
  fun provideNFTDetailsViewModelFactory(data: NFTItem): NFTDetailsViewModelFactory {
    return NFTDetailsViewModelFactory(data)
  }

  @Provides
  fun provideNFTDetailsData(fragment: NFTDetailsFragment): NFTItem {
    fragment.requireArguments()
      .apply {
        return getSerializable(NFTDetailsFragment.NFTITEMDATA)!! as NFTItem
      }
  }

  @Provides
  fun provideNFTDetailsNavigator(fragment: NFTDetailsFragment): NFTDetailsNavigator {
    return NFTDetailsNavigator(fragment.findNavController())
  }
}