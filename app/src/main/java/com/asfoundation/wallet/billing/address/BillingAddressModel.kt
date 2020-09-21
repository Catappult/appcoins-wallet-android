package com.asfoundation.wallet.billing.address

import com.adyen.checkout.core.model.ModelObject
import java.io.Serializable

data class BillingAddressModel(
    val address: String,
    val city: String,
    val zipcode: String,
    val state: String,
    val country: String,
    val number: String,
    val remember: Boolean
)

data class BillingPaymentModel(
    val adyenPaymentMethod: ModelObject,
    val shouldStoreMethod: Boolean,
    val hasCvc: Boolean,
    val supportedShopperInteraction: List<String>,
    val returnUrl: String,
    val value: String,
    val currency: String,
    val reference: String?,
    val paymentType: String,
    val origin: String?,
    val packageName: String,
    val metadata: String?,
    val sku: String?,
    val callbackUrl: String?,
    val transactionType: String,
    val developerWallet: String?
) : Serializable