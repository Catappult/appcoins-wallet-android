package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.util.Error

data class VerificationPaymentModel(val error: Error = Error())
