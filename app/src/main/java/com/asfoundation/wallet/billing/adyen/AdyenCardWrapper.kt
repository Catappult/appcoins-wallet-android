package com.asfoundation.wallet.billing.adyen

import com.adyen.checkout.base.model.payments.request.CardPaymentMethod

data class AdyenCardWrapper(
    val cardPaymentMethod: CardPaymentMethod,
    val shouldStoreCard: Boolean,
    val hasCvc: Boolean,
    val supportedShopperInteractions: List<String>)