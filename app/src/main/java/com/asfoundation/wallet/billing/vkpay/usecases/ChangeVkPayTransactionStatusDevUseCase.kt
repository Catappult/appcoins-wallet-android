package com.asfoundation.wallet.billing.vkpay.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.vkpay.repository.VkPayRepository
import io.reactivex.Single
import javax.inject.Inject

class ChangeVkPayTransactionStatusDevUseCase @Inject constructor(
  private val walletService: WalletService,
  private val vkPayRepository: VkPayRepository,
) {

  operator fun invoke(
    status: String?
  ): Single<Boolean> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        vkPayRepository.changeVkTransactionStatusDev(
          walletAddress = address,
          orderId = address,
          status = status
        )
      }
  }

}