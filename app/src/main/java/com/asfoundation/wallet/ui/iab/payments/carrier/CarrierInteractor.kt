package com.asfoundation.wallet.ui.iab.payments.carrier

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletAddresses
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers

class CarrierInteractor(val repository: CarrierBillingRepository,
                        val walletService: WalletService,
                        val partnerAddressService: AddressService,
                        val inAppPurchaseInteractor: InAppPurchaseInteractor,
                        val logger: Logger) {

  fun createPayment(phoneNumber: String, packageName: String,
                    origin: String?, transactionData: String?, transactionType: String,
                    currency: String,
                    value: String): Single<CarrierPaymentModel> {
    return Single.zip(getAddresses(packageName), getTransactionBuilder(transactionData),
        BiFunction { addrs: WalletAddresses, builder: TransactionBuilder ->
          Pair(addrs, builder)
        })
        .flatMap { pair ->
          repository.makePayment(pair.first.address, pair.first.signedAddress, phoneNumber,
              packageName, origin, pair.second.skuId, pair.second.orderReference, transactionType,
              currency, value, pair.second.toAddress(), pair.first.oemAddress,
              pair.first.storeAddress, pair.first.address)
        }
        .doOnError { e -> logger.log("CarrierInteractor", e) }
  }

  fun getTransactionBuilder(transactionData: String?): Single<TransactionBuilder> {
    return inAppPurchaseInteractor.parseTransaction(transactionData, true)
        .subscribeOn(Schedulers.io())
  }

  fun getAddresses(packageName: String): Single<WalletAddresses> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress()
        .subscribeOn(Schedulers.io()), partnerAddressService.getStoreAddressForPackage(packageName)
        .subscribeOn(Schedulers.io()),
        partnerAddressService.getOemAddressForPackage(packageName)
            .subscribeOn(Schedulers.io()), Function3 { addressModel, storeAddress, oemAddress ->
      return@Function3 WalletAddresses(addressModel.address, addressModel.signedAddress,
          storeAddress, oemAddress)
    })

  }
}