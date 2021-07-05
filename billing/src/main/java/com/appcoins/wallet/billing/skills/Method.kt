package com.appcoins.wallet.billing.skills

class Method(
    var type: String,
    var encryptedCardNumber: String,
    var encryptedExpiryMonth: String,
    var encryptedExpiryYear: String,
    var encryptedSecurityCode: String
)