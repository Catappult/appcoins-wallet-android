package com.asfoundation.wallet.billing.adyen

data class PaymentData(val encryptedCardNumber: String?, val encryptedExpiryMonth: String?,
                       val encryptedExpiryYear: String?, val encryptedSecurityCode: String?,
                       val storeDetails: Boolean = false, val holderName: String? = null)
