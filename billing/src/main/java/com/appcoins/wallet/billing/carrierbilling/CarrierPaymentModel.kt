package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.response.TransactionCarrier
import com.appcoins.wallet.billing.carrierbilling.response.TransactionCarrierError
import com.appcoins.wallet.billing.common.response.TransactionFee
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error

data class CarrierPaymentModel(
    val uid: String,
    val paymentUrl: String,
    val fee: TransactionFee?,
    val carrier: TransactionCarrier?,
    val status: TransactionStatus, val error: TransactionCarrierError?,
    val networkError: Error = Error()
) {
  constructor(error: Error) : this("", "", null, null, TransactionStatus.FAILED, null, error)

  fun hasError(): Boolean {
    return error != null || networkError.hasError
  }
}