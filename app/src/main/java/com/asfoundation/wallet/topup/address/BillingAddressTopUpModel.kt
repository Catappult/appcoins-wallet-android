package com.asfoundation.wallet.topup.address

import com.adyen.checkout.core.model.ModelObject
import java.io.Serializable

data class BillingPaymentTopUpModel(
    val adyenPaymentMethod: ModelObject,
    val shouldStoreMethod: Boolean,
    val hasCvc: Boolean,
    val supportedShopperInteraction: List<String>,
    val returnUrl: String,
    val value: String,
    val currency: String,
    val paymentType: String,
    val transactionType: String,
    val packageName: String
) : Serializable