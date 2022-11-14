package com.asfoundation.wallet.billing.paypal

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.billing.PayPalV2Repository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.redeem_gift.repository.RedeemCode
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
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