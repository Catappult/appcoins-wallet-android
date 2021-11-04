package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase
import dagger.Module
import dagger.Provides

@Module
class NFTModule {
    @Provides
    fun provideNFTViewModelFactory(getNFTListUseCase: GetNFTListUseCase): NFTViewModelFactory {
        return NFTViewModelFactory(getNFTListUseCase)
    }

    @Provides
    fun provideNFTNavigator(fragment: NFTFragment): NFTNavigator {
        return NFTNavigator(fragment.findNavController())
    }
}