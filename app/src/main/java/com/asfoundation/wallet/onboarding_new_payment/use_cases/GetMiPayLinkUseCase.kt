package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.MiPayTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Single
import javax.inject.Inject

class GetMiPayLinkUseCase @Inject constructor(
  private val walletService: WalletService,
  private val remoteRepository: RemoteRepository,
  private val partnerAddressService: AddressService,
) {
  operator fun invoke(
    data: TransactionBuilder,
    amount: String,
    paymentType: String,
    currency: String,
    packageName: String,
    returnUrl: String
  ): Single<MiPayTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        partnerAddressService.getAttribution(packageName)
          .flatMap { attributionEntity ->
            walletService.signContent(address).flatMap { signature ->
              remoteRepository.createMiPayTransaction(
                paymentId = paymentType,
                packageName = packageName,
                price = amount,
                currency = currency,
                productName = data.skuId,
                type = data.type,
                callback = data.callbackUrl,
                referrerUrl = data.referrerUrl,
                walletAddress = address,
                entityOemId = attributionEntity.oemId,
                returnUrl = returnUrl,
                walletSignature = signature,
                orderReference = data.orderReference,
                guestWalletId = data.guestWalletId
              )
            }
          }
      }
  }
}
