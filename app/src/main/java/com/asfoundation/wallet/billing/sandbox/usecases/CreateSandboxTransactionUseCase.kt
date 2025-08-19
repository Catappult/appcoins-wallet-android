package com.asfoundation.wallet.billing.sandbox.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.SandboxTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.sandbox.repository.SandboxRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateSandboxTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val sandboxRepository: SandboxRepository,
) {

  operator fun invoke(
    value: String,
    currency: String,
    reference: String?,
    origin: String?,
    packageName: String,
    metadata: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    referrerUrl: String?,
    guestWalletId: String?,
  ): Single<SandboxTransaction> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress(),
      partnerAddressService.getAttribution(packageName)
    )
    { walletAddressModel, attributionEntity -> Pair(walletAddressModel, attributionEntity) }
      .flatMap { pair ->
        val addressModel = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase()
          .flatMap { promoCode ->
            sandboxRepository.createTransaction(
              value = value,
              currency = currency,
              reference = reference,
              walletAddress = addressModel.address,
              walletSignature = addressModel.signedAddress,
              origin = origin,
              packageName = packageName,
              metadata = metadata,
              sku = sku,
              callbackUrl = callbackUrl,
              transactionType = transactionType,
              entityOemId = attrEntity.oemId,
              entityDomain = attrEntity.domain,
              entityPromoCode = promoCode.code,
              userWallet = addressModel.address,
              referrerUrl = referrerUrl,
              guestWalletId = guestWalletId,
            )
          }
      }
  }

}
