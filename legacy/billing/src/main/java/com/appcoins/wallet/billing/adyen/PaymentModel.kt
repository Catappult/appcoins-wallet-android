package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.payments.response.Action
import com.appcoins.wallet.core.network.microservices.model.TransactionResponse
import com.appcoins.wallet.billing.util.Error
import java.io.Serializable

data class PaymentModel(
  val resultCode: String?,
  val refusalReason: String?,
  val refusalCode: Int?,
  val action: Action?,
  val redirectUrl: String?,
  val paymentData: String?,
  val uid: String,
  val purchaseUid: String?,
  val hash: String?,
  val orderReference: String?,
  val fraudResultIds: List<Int>,
  val status: Status,
  val errorMessage: String? = null,
  val errorCode: Int? = null,
  val error: Error = Error()
) : Serializable {

  constructor(error: Error) : this(
    "", null, null, null, "", "", "", null, "", "", emptyList(),
    Status.FAILED, null, null, error
  )

  constructor(response: TransactionResponse, status: Status) : this(
    "", null, null, null, "", "",
    response.uid, null, response.hash, response.orderReference, emptyList(), status,
    response.metadata?.errorMessage, response.metadata?.errorCode
  )

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, SETTLED, PROCESSING, COMPLETED, PENDING_USER_PAYMENT,
    INVALID_TRANSACTION, FAILED, CANCELED, FRAUD
  }
}
