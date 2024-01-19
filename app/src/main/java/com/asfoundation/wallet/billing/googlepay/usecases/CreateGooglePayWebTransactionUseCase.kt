package com.asfoundation.wallet.billing.googlepay.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import com.asfoundation.wallet.entity.TransactionBuilder

import io.reactivex.Single
import javax.inject.Inject

class CreateGooglePayWebTransactionUseCase @Inject constructor(
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val googlePayWebRepository: GooglePayWebRepository,
) {

  operator fun invoke(
    value: String,
    currency: String,
    origin: String?,
    method: String,
    returnUrl: String,
    transactionBuilder: TransactionBuilder,
  ): Single<GooglePayWebTransaction> {
    return Single.zip(walletService.getWalletAddress(),
      partnerAddressService.getAttribution(transactionBuilder.domain),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val address = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          googlePayWebRepository.createTransaction(
            value = value,
            currency = currency,
            reference = transactionBuilder.orderReference,
            walletAddress = address,
            origin = origin,
            packageName = transactionBuilder.domain,
            metadata = transactionBuilder.payload,
            method = method,
            sku = transactionBuilder.skuId,
            callbackUrl = transactionBuilder.callbackUrl,
            transactionType = transactionBuilder.type,
            developerWallet = transactionBuilder.toAddress(),
            entityOemId = attrEntity.oemId,
            entityDomain = attrEntity.domain,
            entityPromoCode = promoCode.code,
            userWallet = address,
            referrerUrl = transactionBuilder.referrerUrl,
            returnUrl = returnUrl,
          )
        }
      }
  }

}