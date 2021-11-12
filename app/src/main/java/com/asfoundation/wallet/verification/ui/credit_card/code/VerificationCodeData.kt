package com.asfoundation.wallet.verification.ui.credit_card.code

data class VerificationCodeData(val loaded: Boolean,
                                val date: Long?,
                                val format: String?,
                                val amount: String?,
                                val currency: String?,
                                val symbol: String?,
                                val period: String?,
                                val digits: Int?
)