package com.asfoundation.wallet.verification.credit_card.code

import com.appcoins.wallet.billing.util.Error

data class VerificationInfoModel(val date: Long?,
                                 val format: String?,
                                 val amount: String?,
                                 val currency: String?,
                                 val symbol: String?,
                                 val period: String?,
                                 val digits: Int?,
                                 val error: Error = Error()) {

  constructor(error: Error) : this(null, null, null, null, null, null, null, error)
}