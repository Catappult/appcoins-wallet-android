package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import com.asfoundation.wallet.billing.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(value: String, currency: String, reference: String?,
                  origin: String?, packageName: String, metadata: String?,
                  sku: String?, callbackUrl: String?, transactionType: String,
                  developerWallet: String?,
                  referrerUrl: String?): Single<PaypalTransaction> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress(),
      partnerAddressService.getAttributionEntity(packageName),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val addressModel = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          payPalV2Repository.createTransaction(
            value = value,
            currency = currency,
            reference = reference,
            walletAddress = addressModel.address,
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
            userWallet = addressModel.address,
            walletSignature = addressModel.signedAddress,
            referrerUrl = referrerUrl
          )
        }
      }
  }

}