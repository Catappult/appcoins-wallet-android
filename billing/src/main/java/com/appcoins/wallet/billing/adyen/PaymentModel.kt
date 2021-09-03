package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.payments.response.Action
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error

data class PaymentModel(val resultCode: String?, val refusalReason: String?, val refusalCode: Int?,
                        val action: Action?, val redirectUrl: String?,
                        val paymentData: String?, val uid: String,
                        val hash: String?, val orderReference: String?,
                        val fraudResultIds: List<Int>, val status: TransactionStatus,
                        val errorMessage: String?, val errorCode: Int?,
                        val error: Error = Error()) {

  constructor(error: Error) : this("", null, null, null, "", "", "", null, "", emptyList(),
      TransactionStatus.FAILED, null, null, error)

  constructor(response: TransactionResponse) : this("", null, null, null, "", "",
      response.uid, response.hash, response.orderReference, emptyList(), response.status,
      response.metadata?.errorMessage, response.metadata?.errorCode)
}
