package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(
    value: String, currency: String, reference: String?,
    origin: String?, packageName: String, metadata: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    referrerUrl: String?, guestWalletId: String?
  ): Single<PaypalTransaction> {
    return Single.zip(walletService.getWalletAddress(),
      partnerAddressService.getAttribution(packageName),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          payPalV2Repository.createTransaction(
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
            entityOemId = attrEntity.oemId,
            entityDomain = attrEntity.domain,
            entityPromoCode = promoCode.code,
            userWallet = address,
            referrerUrl = referrerUrl,
            guestWalletId = guestWalletId,
          )
        }
      }
  }

}