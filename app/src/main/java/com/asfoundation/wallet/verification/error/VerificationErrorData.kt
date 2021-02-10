package com.asfoundation.wallet.verification.error

import com.appcoins.wallet.billing.adyen.VerificationCodeResult

data class VerificationErrorData(val errorType: VerificationCodeResult.ErrorType,
                                 val amount: String,
                                 val symbol: String)