package com.asfoundation.wallet.nfts.usecases


import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single

class GetNFTListUseCase(private val getCurrentWallet: GetCurrentWalletUseCase,
                        private val NFTRepository: NFTRepository
) {

  operator fun invoke(): Single<List<NFTItem>> {
    return getCurrentWallet()
        .flatMap { wallet ->
          NFTRepository.getNFTAssetList(wallet.address)
        }
  }
}
