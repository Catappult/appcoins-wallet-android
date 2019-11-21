package com.asfoundation.wallet.billing.adyen

data class PaymentData(val encryptedCardNumber: String?, val encryptedExpiryMonth: String?,
                       val encryptedExpiryYear: String?, val encryptedSecurityCode: String?,
                       val holderName: String?)
