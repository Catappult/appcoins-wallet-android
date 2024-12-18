package com.asfoundation.wallet.billing.amazonPay.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateAmazonPayTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val amazonPayRepository: AmazonPayRepository
) {

  operator fun invoke(
    price: AmazonPrice, reference: String?,
    origin: String?, metadata: String?, packageName: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    referrerUrl: String?, chargePermissionId: String?, guestWalletId: String?
  ): Single<AmazonPayTransaction> {
    return Single.zip(
      walletService.getWalletAddress(),
      partnerAddressService.getAttribution(packageName ?: "")
    ) { address, attributionEntity -> Pair(address, attributionEntity) }
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          amazonPayRepository.createTransaction(
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
            referrerUrl = referrerUrl,
            method = METHOD_AMAZONPAY,
            chargePermissionId = chargePermissionId,
            guestWalletId = guestWalletId
          )
        }
      }
  }

  private companion object {
    private const val METHOD_AMAZONPAY= "amazonpay"
  }

}