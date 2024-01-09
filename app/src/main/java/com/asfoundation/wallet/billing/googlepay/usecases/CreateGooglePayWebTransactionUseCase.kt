package com.asfoundation.wallet.billing.googlepay.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository

import io.reactivex.Single
import javax.inject.Inject

class CreateGooglePayWebTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val googlePayWebRepository: GooglePayWebRepository,
) {

  operator fun invoke(
    value: String, currency: String, reference: String?,
    origin: String?, packageName: String, metadata: String?, method: String,
    sku: String?, callbackUrl: String?, transactionType: String,
    developerWallet: String?,
    referrerUrl: String?,
    returnUrl: String,
  ): Single<GooglePayWebTransaction> {
    return Single.zip(walletService.getWalletAddress(),
      partnerAddressService.getAttribution(packageName),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          googlePayWebRepository.createTransaction(
            value = value,
            currency = currency,
            reference = reference,
            walletAddress = address,
            origin = origin,
            packageName = packageName,
            metadata = metadata,
            method = method,
            sku = sku,
            callbackUrl = callbackUrl,
            transactionType = transactionType,
            developerWallet = developerWallet,
            entityOemId = attrEntity.oemId,
            entityDomain = attrEntity.domain,
            entityPromoCode = promoCode.code,
            userWallet = address,
            referrerUrl = referrerUrl,
            returnUrl = returnUrl,
          )
        }
      }
  }

}