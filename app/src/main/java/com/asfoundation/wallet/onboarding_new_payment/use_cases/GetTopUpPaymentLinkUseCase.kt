package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentData
import io.reactivex.Single
import javax.inject.Inject

class GetTopUpPaymentLinkUseCase @Inject constructor(
    private val walletService: WalletService,
    private val remoteRepository: RemoteRepository,
) {
    operator fun invoke(
        data: LocalTopUpPaymentData,
        topUpTitle: String
    ) : Single<String> {
// call topUpTitle context?.getString(R.string.topup_title) ?: "Top up"
        return walletService.getAndSignCurrentWalletAddress()
            .flatMap { walletAddressModel ->
                remoteRepository.createLocalPaymentTransaction(data.packageName, data.topUpData.fiatValue,
                    data.topUpData.fiatCurrencyCode, data.paymentId,
                    topUpTitle,
                    TOP_UP_TRANSACTION_TYPE,
                    null, null, null, null, null, null, null, null, null,
                    walletAddressModel.address, walletAddressModel.signedAddress)
            }
            .map { it.url }
    }
    companion object {
        private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
    }
}




