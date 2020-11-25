package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.payments.response.Action
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error

data class PaymentModel(val resultCode: String?, val refusalReason: String?, val refusalCode: Int?,
                        val action: Action?, val redirectUrl: String?,
                        val paymentData: String?, val uid: String,
                        val hash: String?, val orderReference: String?,
                        val status: TransactionStatus, val errorMessage: String?,
                        val errorCode: Int?, val error: Error = Error()) {

  constructor(error: Error) : this("", null, null, null, "", "", "", null, "",
      TransactionStatus.FAILED, null, null, error)
}
