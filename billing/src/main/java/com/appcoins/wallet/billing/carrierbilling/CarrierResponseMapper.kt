package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.response.CarrierTransactionResponse
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.isNoNetworkException

class CarrierResponseMapper {

  fun mapPayment(response: CarrierTransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(response.uid, response.url, response.fee, response.carrier,
        response.status, response.metadata?.errorMessage, response.metadata?.errorCode, Error())
  }

  fun mapPaymentError(throwable: Throwable): CarrierPaymentModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    return CarrierPaymentModel(
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second))
  }
}