package com.asfoundation.wallet.billing.vkpay.usecases

import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.vkpay.repository.VkPayRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateVkPayTransactionTopUpUseCase @Inject constructor(
  private val walletService: WalletService,
  private val vkPayRepository: VkPayRepository,
) {

  operator fun invoke(
    price: VkPrice,
    email: String,
    phone: String
  ): Single<VkPayTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        vkPayRepository.createTransaction(
          price = price,
          reference = null,
          walletAddress = address,
          origin = null,
          packageName = BuildConfig.APPLICATION_ID,
          metadata = null,
          sku = null,
          callbackUrl = null,
          transactionType = TOP_UP_TRANSACTION_TYPE,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = null,
          guestWalletId = null,
          method = METHOD_VK_PAY,
          email = email,
          phone = phone
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
    private const val METHOD_VK_PAY = "vk_pay"
  }

}