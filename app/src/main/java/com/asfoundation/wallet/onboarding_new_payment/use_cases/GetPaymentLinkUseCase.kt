package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentData
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentLinkUseCase @Inject constructor(
    private val walletService: WalletService,
    private val remoteRepository: RemoteRepository,
    private val partnerAddressService: AddressService,
    private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
) {
    operator fun invoke(
        data: LocalPaymentData,
    ) : Single<String> {
        return walletService.getAndSignCurrentWalletAddress()
            .flatMap { walletAddressModel ->
                partnerAddressService.getAttributionEntity(data.packageName)
                    .flatMap { attributionEntity ->
                        getCurrentPromoCodeUseCase().flatMap { promoCode ->
                            remoteRepository.createLocalPaymentTransaction(data.paymentId, data.packageName,
                                data.fiatAmount, data.currency, data.skuId, data.type, data.origin, data.developerAddress,
                                attributionEntity.oemId, attributionEntity.domain, promoCode.code,
                                data.payload,
                                data.callbackUrl, data.orderReference,
                                data.referrerUrl, walletAddressModel.address, walletAddressModel.signedAddress)
                        }
                    }
                    .map { it.url }
            }
    }
}




