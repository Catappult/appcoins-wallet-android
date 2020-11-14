package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.response.CarrierCreateTransactionResponse
import com.appcoins.wallet.billing.carrierbilling.response.CarrierTransactionErrorResponse
import com.appcoins.wallet.billing.carrierbilling.response.TransactionCarrierError
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit

class CarrierResponseMapper(private val retrofit: Retrofit) {

  fun mapPayment(response: CarrierCreateTransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(response.uid, null, null, response.url, response.fee,
        response.carrier, response.status, null, Error())
  }

  fun mapPayment(response: TransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(response.uid, response.hash, response.orderReference, null, null,
        null, response.status, null, Error())
  }

  fun mapPaymentError(throwable: Throwable): CarrierPaymentModel {
    throwable.printStackTrace()

    var carrierError: TransactionCarrierError? = null
    (throwable as HttpException).response()
        ?.errorBody()
        ?.let { body ->
          val errorConverter: Converter<ResponseBody, CarrierTransactionErrorResponse> = retrofit
              .responseBodyConverter(CarrierTransactionErrorResponse::class.java,
                  arrayOfNulls<Annotation>(0))
          carrierError = try {
            errorConverter.convert(body)?.error
          } catch (e: Exception) {
            null
          }
        }

    val codeAndMessage = throwable.getErrorCodeAndMessage()
    return CarrierPaymentModel(carrierError,
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second))
  }
}