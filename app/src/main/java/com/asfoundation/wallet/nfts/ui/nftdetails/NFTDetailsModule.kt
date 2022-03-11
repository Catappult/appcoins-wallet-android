package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.nfts.domain.NFTItem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Named

@InstallIn(FragmentComponent::class)
@Module
class NFTDetailsModule {
  @Provides
  fun provideNFTDetailsViewModelFactory(
    @Named("NFTDetailsItem") data: NFTItem
  ): NFTDetailsViewModelFactory {
    return NFTDetailsViewModelFactory(data)
  }

  @Provides
  @Named("NFTDetailsItem")
  fun provideNFTDetailsData(fragment: Fragment): NFTItem {
    fragment.requireArguments()
      .apply {
        return getSerializable(NFTDetailsFragment.NFT_ITEM_DATA)!! as NFTItem
      }
  }
}