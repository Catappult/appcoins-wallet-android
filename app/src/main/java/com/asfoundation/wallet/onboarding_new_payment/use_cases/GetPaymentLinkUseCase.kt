package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentLinkUseCase @Inject constructor(
  private val walletService: WalletService,
  private val remoteRepository: RemoteRepository,
  private val partnerAddressService: AddressService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
) {
  operator fun invoke(
    data: TransactionBuilder,
    amount: String,
    paymentType: String,
    currency: String,
    packageName: String,
  ): Single<Transaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        partnerAddressService.getAttribution(packageName)
          .flatMap { attributionEntity ->
            getCurrentPromoCodeUseCase().flatMap { promoCode ->
              remoteRepository.createLocalPaymentTransaction(
                paymentId = paymentType,
                packageName = packageName,
                price = amount,
                currency = currency,
                productName = data.skuId,
                type = data.type,
                origin = data.origin,
                entityOemId = attributionEntity.oemId,
                entityDomain = attributionEntity.domain,
                entityPromoCode = promoCode.code,
                developerPayload = data.payload,
                callback = data.callbackUrl,
                orderReference = data.orderReference,
                referrerUrl = data.referrerUrl,
                walletAddress = address
              )
            }
          }
      }
  }
}
