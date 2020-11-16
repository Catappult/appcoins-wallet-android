package com.asfoundation.wallet.wallet_verification.code

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository

class WalletVerificationCodeInteractor(
    private val adyenPaymentRepository: AdyenPaymentRepository
)