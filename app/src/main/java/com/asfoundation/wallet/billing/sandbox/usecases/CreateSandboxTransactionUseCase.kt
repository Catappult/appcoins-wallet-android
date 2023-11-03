package com.asfoundation.wallet.billing.sandbox.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.SandboxTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.sandbox.repository.SandboxRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateSandboxTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase,
  private val sandboxRepository: SandboxRepository,
) {

  operator fun invoke(
    value: String, currency: String, reference: String?,
    origin: String?, packageName: String, metadata: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    developerWallet: String?,
    referrerUrl: String?
  ): Single<SandboxTransaction> {
    return Single.zip(walletService.getWalletAddress(),
      partnerAddressService.getAttributionEntity(packageName),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          sandboxRepository.createTransaction(
            value = value,
            currency = currency,
            reference = reference,
            walletAddress = address,
            origin = origin,
            packageName = packageName,
            metadata = metadata,
            sku = sku,
            callbackUrl = callbackUrl,
            transactionType = transactionType,
            developerWallet = developerWallet,
            entityOemId = attrEntity.oemId,
            entityDomain = attrEntity.domain,
            entityPromoCode = promoCode.code,
            userWallet = address,
            referrerUrl = referrerUrl
          )
        }
      }
  }

}
