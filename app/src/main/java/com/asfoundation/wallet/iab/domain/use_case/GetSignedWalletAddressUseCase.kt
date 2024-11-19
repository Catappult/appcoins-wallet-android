package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetSignedWalletAddressUseCase @Inject constructor(
  private val walletService: WalletService,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
  ) {

  suspend operator fun invoke(walletAddress: String) =
    walletService.getAndSignWalletAddress(walletAddress = walletAddress).callAsync(networkDispatcher)
}