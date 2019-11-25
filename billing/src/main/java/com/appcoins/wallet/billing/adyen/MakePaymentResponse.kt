package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.payments.response.RedirectAction

data class MakePaymentResponse(val resultCode: String, val action: RedirectAction?,
                               val refusalReason: String?, val refusalReasonCode: String?)