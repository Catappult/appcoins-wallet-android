package com.asfoundation.wallet.nfts.usecases

import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.nfts.domain.GasInfo
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import com.github.michaelbull.result.get
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class EstimateNFTSendGasUseCase @Inject constructor(
  private val getCurrentWallet: GetCurrentWalletUseCase,
  private val getSelectedCurrencyUseCase: com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase,
  private val NFTRepository: NFTRepository,
  private val dispatchers: Dispatchers
) {

  operator fun invoke(item: NFTItem, toAddress: String): Single<GasInfo> {
    return getCurrentWallet()
      .flatMap { wallet ->
        rxSingle(dispatchers.io) { getSelectedCurrencyUseCase(bypass = false) }
          .flatMap { selected ->
            return@flatMap NFTRepository.estimateSendNFTGas(
              wallet.address, toAddress, item.tokenId,
              item.contractAddress, item.schema, selected.get()!!
            )
          }
      }
  }
}