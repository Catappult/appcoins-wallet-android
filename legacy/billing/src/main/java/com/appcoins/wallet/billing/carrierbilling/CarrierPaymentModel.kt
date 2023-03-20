package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.core.network.microservices.model.TransactionCarrier
import com.appcoins.wallet.core.network.microservices.model.TransactionFee
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import java.math.BigDecimal

data class CarrierPaymentModel(
    val uid: String,
    val hash: String?,
    val reference: String?,
    val paymentUrl: String?,
    val fee: TransactionFee?,
    val carrier: TransactionCarrier?,
    val purchaseUid: String?,
    val status: TransactionStatus, val error: CarrierError
) {
  constructor(error: CarrierError) : this("", "", null, null, null, null, null,
      TransactionStatus.FAILED, error)

}

sealed class CarrierError(val errorCode: Int?, val errorMessage: String?)

object NoError : CarrierError(null, null)

data class GenericError(private val isNetworkError: Boolean = false, private val httpCode: Int?,
                        private val message: String?) : CarrierError(httpCode, message)

data class ForbiddenError(private val httpCode: Int?, private val message: String?,
                          val type: ForbiddenType) : CarrierError(httpCode, message) {
  enum class ForbiddenType { BLOCKED, SUB_ALREADY_OWNED }
}

data class InvalidPriceError(private val httpCode: Int?, private val message: String?,
                             val type: BoundType, val value: BigDecimal) :
    CarrierError(httpCode, message) {
  enum class BoundType { UPPER, LOWER }
}

data class InvalidPhoneNumber(private val httpCode: Int?, private val message: String?) :
    CarrierError(httpCode, message)