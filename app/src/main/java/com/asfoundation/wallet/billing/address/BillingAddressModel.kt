package com.asfoundation.wallet.billing.address

import java.io.Serializable

data class BillingAddressModel(
    val address: String,
    val city: String,
    val zipcode: String,
    val state: String,
    val country: String,
    val number: String,
    val remember: Boolean
) : Serializable