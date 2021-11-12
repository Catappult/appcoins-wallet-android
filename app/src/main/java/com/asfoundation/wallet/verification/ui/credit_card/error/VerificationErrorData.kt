package com.asfoundation.wallet.verification.ui.credit_card.error

import com.appcoins.wallet.billing.adyen.VerificationCodeResult

data class VerificationErrorData(val errorType: VerificationCodeResult.ErrorType,
                                 val amount: String,
                                 val symbol: String)