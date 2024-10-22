package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBalanceUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val walletInfoRepository: WalletInfoRepository,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
) {

  suspend operator fun invoke(): WalletInfo {
    val wallet = walletRepository.getDefaultWallet().callAsync(networkDispatcher)

    return walletInfoRepository.getCachedWalletInfo(wallet.address).callAsync(networkDispatcher)
  }
}

