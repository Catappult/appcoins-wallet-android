package com.asfoundation.wallet.nfts.usecases

import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single

class EstimateNFTSendGasUseCase(private val getCurrentWallet: GetCurrentWalletUseCase,
                                private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
                                private val NFTRepository: NFTRepository) {

  operator fun invoke(item: NFTItem, toAddress: String): Single<GasInfo> {
    return getCurrentWallet().flatMap { wallet ->
      getSelectedCurrencyUseCase(bypass = false).flatMap { selected ->
        return@flatMap NFTRepository.estimateSendNFTGas(wallet.address, toAddress, item.tokenId,
            item.contractAddress, item.schema, selected)
      }
    }
  }
}