package com.asfoundation.wallet.billing.address

data class BillingAddressModel(
    val address: String?,
    val city: String?,
    val zipcode: String?,
    val state: String?,
    val country: String?,
    val remember: Boolean
)