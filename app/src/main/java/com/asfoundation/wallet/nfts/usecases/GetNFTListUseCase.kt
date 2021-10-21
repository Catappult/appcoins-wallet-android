package com.asfoundation.wallet.nfts.usecases


import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.repository.NftRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single

class GetNFTListUseCase(private val getCurrentWallet: GetCurrentWalletUseCase, private val nftRepository: NftRepository
) {

  operator fun invoke(): Single<List<NFTItem>> {
    return getCurrentWallet()
      .flatMap {
        wallet -> nftRepository.getNFTAssetList(wallet.address)
      }
  }
}

//NFTsController
//Navegação desde MyWallets
