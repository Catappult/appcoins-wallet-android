package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
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
    ) : Single<Transaction> {
        return walletService.getWalletAddress()
            .flatMap { address ->
                partnerAddressService.getAttributionEntity(packageName)
                    .flatMap { attributionEntity ->
                        getCurrentPromoCodeUseCase().flatMap { promoCode ->
                            remoteRepository.createLocalPaymentTransaction(paymentType, packageName,
                                amount, currency, data.skuId, data.type, data.origin, data.toAddress(),
                                attributionEntity.oemId, attributionEntity.domain, promoCode.code,
                                data.payload,
                                data.callbackUrl, data.orderReference,
                                data.referrerUrl, address)
                        }
                    }
            }
    }
}




