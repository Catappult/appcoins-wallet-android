package com.asfoundation.wallet.verification.ui.credit_card.intro

import com.appcoins.wallet.billing.adyen.PaymentInfoModel

data class VerificationIntroModel(val verificationInfoModel: VerificationInfoModel,
                                  val paymentInfoModel: PaymentInfoModel)

data class VerificationInfoModel(val currency: String,
                                 val symbol: String,
                                 val value: String,
                                 val digits: Int,
                                 val format: String,
                                 val period: String)