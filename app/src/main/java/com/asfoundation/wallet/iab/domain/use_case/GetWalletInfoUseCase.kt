package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetWalletInfoUseCase @Inject constructor(
  private val walletService: WalletService,
  @IoDispatcher val networkDispatcher: CoroutineDispatcher,
  ) {

  suspend operator fun invoke() =
    walletService.getWalletAddress().callAsync(networkDispatcher)
}