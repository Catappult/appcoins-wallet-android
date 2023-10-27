package com.asfoundation.wallet.billing.vkpay.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.asfoundation.wallet.billing.vkpay.repository.VkPayRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateVkPayTransactionUseCase @Inject constructor(
  private val walletService: WalletService,
  private val vkPayRepository: VkPayRepository,
) {

  operator fun invoke(
    price: VkPrice, reference: String?,
    origin: String?, metadata: String?, packageName: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    developerWallet: String?,
    referrerUrl: String?
  ): Single<VkPayTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        vkPayRepository.createTransaction(
          price = price,
          reference = reference,
          walletAddress = address,
          origin = origin,
          packageName = packageName,
          metadata = metadata,
          sku = sku,
          callbackUrl = callbackUrl,
          transactionType = transactionType,
          developerWallet = developerWallet,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = referrerUrl,
          method = METHOD_VK_PAY
        )
      }
  }

  private companion object {
    private const val METHOD_VK_PAY = "vk_pay"
  }

}