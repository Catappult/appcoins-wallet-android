package com.asfoundation.wallet.billing.address

import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.topup.address.BillingPaymentTopUpModel
import io.reactivex.Single

class BillingAddressInteractor(private val adyenPaymentInteractor: AdyenPaymentInteractor) {

  fun makePayment(model: BillingPaymentModel,
                  billingPaymentModel: BillingAddressModel): Single<PaymentModel> {
    val adyenBillingAddress =
        AdyenBillingAddress(billingPaymentModel.address, billingPaymentModel.city,
            billingPaymentModel.zipcode, billingPaymentModel.number, billingPaymentModel.state,
            billingPaymentModel.country)
    return adyenPaymentInteractor.makePayment(model.adyenPaymentMethod,
        billingPaymentModel.remember,
        model.hasCvc, model.supportedShopperInteraction, model.returnUrl, model.value,
        model.currency, model.reference, model.paymentType, model.origin, model.packageName,
        model.metadata, model.sku, model.callbackUrl, model.transactionType, model.developerWallet,
        adyenBillingAddress)
  }

  fun makeTopUpPayment(model: BillingPaymentTopUpModel,
                       billingPaymentModel: BillingAddressModel): Single<PaymentModel> {
    val adyenBillingAddress =
        AdyenBillingAddress(billingPaymentModel.address, billingPaymentModel.city,
            billingPaymentModel.zipcode, billingPaymentModel.number, billingPaymentModel.state,
            billingPaymentModel.country)
    return adyenPaymentInteractor.makeTopUpPayment(model.adyenPaymentMethod,
        billingPaymentModel.remember, model.hasCvc, model.supportedShopperInteraction,
        model.returnUrl, model.value, model.currency, model.paymentType, model.transactionType,
        model.packageName, adyenBillingAddress)
  }

}