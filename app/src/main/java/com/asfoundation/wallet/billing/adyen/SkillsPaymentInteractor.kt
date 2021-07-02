package com.asfoundation.wallet.billing.adyen

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.asfoundation.wallet.billing.partners.AddressService
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class SkillsPaymentInteractor(
  private val skillsPaymentRepository: SkillsPaymentRepository,
  private val partnerAddressService: AddressService,
  private val walletService: WalletService
) {

  fun makeSkillsPayment(
    returnUrl: String,
    packageName: String,
    productToken: String,
    encryptedCardNumber: String?,
    encryptedExpiryMonth: String?,
    encryptedExpiryYear: String?,
    encryptedSecurityCode: String
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { address ->
        Single.zip(
          partnerAddressService.getStoreAddressForPackage(packageName),
          partnerAddressService.getOemAddressForPackage(packageName),
          BiFunction { storeAddress: String, oemAddress: String ->
            Pair(storeAddress, oemAddress)
          })
          .flatMap {
            skillsPaymentRepository.makeSkillsPayment(
              returnUrl,
              address.address,
              address.signedAddress, productToken, encryptedCardNumber, encryptedExpiryMonth,
              encryptedExpiryYear, encryptedSecurityCode
            )
          }
      }
  }

}