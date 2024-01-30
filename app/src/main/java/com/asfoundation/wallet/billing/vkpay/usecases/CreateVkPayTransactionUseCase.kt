package com.asfoundation.wallet.billing.vkpay.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.vkpay.repository.VkPayRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateVkPayTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val vkPayRepository: VkPayRepository,
) {

  operator fun invoke(
    price: VkPrice, reference: String?,
    origin: String?, metadata: String?, packageName: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    referrerUrl: String?,
    email: String,
    phone: String
  ): Single<VkPayTransaction> {
    return Single.zip(
      walletService.getWalletAddress(),
      partnerAddressService.getAttribution(packageName ?: "")
    ) { address, attributionEntity -> Pair(address, attributionEntity) }
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
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
            entityOemId = attrEntity.oemId,
            entityDomain = attrEntity.domain,
            entityPromoCode = promoCode.code,
            userWallet = address,
            referrerUrl = referrerUrl,
            method = METHOD_VK_PAY,
            email = email,
            phone = phone
          )
        }
      }
  }

  private companion object {
    private const val METHOD_VK_PAY = "vk_pay"
  }

}