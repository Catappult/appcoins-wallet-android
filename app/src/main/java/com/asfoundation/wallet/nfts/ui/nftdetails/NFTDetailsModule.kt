package com.asfoundation.wallet.nfts.ui.nftdetails

import com.asfoundation.wallet.nfts.domain.NFTItem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class NFTDetailsModule {
  @Provides
  fun provideNFTDetailsViewModelFactory(data: NFTItem): NFTDetailsViewModelFactory {
    return NFTDetailsViewModelFactory(data)
  }
}