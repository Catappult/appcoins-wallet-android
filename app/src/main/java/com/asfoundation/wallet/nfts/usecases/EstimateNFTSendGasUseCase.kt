package com.asfoundation.wallet.nfts.usecases

import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single
import java.math.BigInteger

class EstimateNFTSendGasUseCase(private val getCurrentWallet: GetCurrentWalletUseCase,
                                private val NFTRepository: NFTRepository) {

  operator fun invoke(item: NFTItem, toAddress: String): Single<Pair<BigInteger, BigInteger>> {
    return getCurrentWallet().flatMap { wallet ->
      return@flatMap NFTRepository.estimateSendNFTGas(wallet.address, toAddress, item.tokenId,
          item.contractAddress, item.schema)
    }
  }
}