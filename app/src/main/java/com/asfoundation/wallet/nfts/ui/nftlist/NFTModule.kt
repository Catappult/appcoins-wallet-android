package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class NFTModule {
  @Provides
  fun provideNFTViewModelFactory(getNFTListUseCase: GetNFTListUseCase): NFTViewModelFactory {
    return NFTViewModelFactory(getNFTListUseCase)
  }

  @Provides
  fun provideNFTNavigator(fragment: Fragment): NFTNavigator {
    return NFTNavigator(fragment.findNavController())
  }
}