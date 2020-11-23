package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.response.TransactionCarrier
import com.appcoins.wallet.billing.common.response.TransactionFee
import com.appcoins.wallet.billing.common.response.TransactionStatus
import java.math.BigDecimal

data class CarrierPaymentModel(
    val uid: String,
    val hash: String?,
    val reference: String?,
    val paymentUrl: String?,
    val fee: TransactionFee?,
    val carrier: TransactionCarrier?,
    val status: TransactionStatus, val error: CarrierError
) {
  constructor(error: CarrierError) : this("", "", null, null, null, null,
      TransactionStatus.FAILED, error)

}

sealed class CarrierError(val errorCode: Int?, val errorMessage: String?)

object NoError : CarrierError(null, null)

data class GenericError(private val isNetworkError: Boolean = false, private val httpCode: Int?,
                        private val message: String?) : CarrierError(httpCode, message)

data class InvalidPriceError(private val httpCode: Int?, private val message: String?,
                             val type: BoundType, val value: BigDecimal) :
    CarrierError(httpCode, message) {
  enum class BoundType { UPPER, LOWER }
}

data class InvalidPhoneNumber(private val httpCode: Int?, private val message: String?) :
    CarrierError(httpCode, message)