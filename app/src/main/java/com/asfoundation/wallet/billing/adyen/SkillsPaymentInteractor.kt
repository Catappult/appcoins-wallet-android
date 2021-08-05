package com.asfoundation.wallet.billing.adyen

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import io.reactivex.Single

class SkillsPaymentInteractor(
    private val skillsPaymentRepository: SkillsPaymentRepository,
    private val walletService: WalletService
) {

  fun makeSkillsPayment(returnUrl: String, productToken: String,
                        encryptedCardNumber: String?, encryptedExpiryMonth: String?,
                        encryptedExpiryYear: String?, encryptedSecurityCode: String
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { address ->
          skillsPaymentRepository.makeSkillsPayment(returnUrl, address.address,
              address.signedAddress, productToken, encryptedCardNumber, encryptedExpiryMonth,
              encryptedExpiryYear, encryptedSecurityCode
          )
        }
  }
}