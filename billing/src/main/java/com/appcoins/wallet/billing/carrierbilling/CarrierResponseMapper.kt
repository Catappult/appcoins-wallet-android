package com.appcoins.wallet.billing.carrierbilling

import com.appcoins.wallet.billing.carrierbilling.response.CarrierCreateTransactionResponse
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponse
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.util.isNoNetworkException
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit

class CarrierResponseMapper(private val retrofit: Retrofit) {

  fun mapPayment(response: CarrierCreateTransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(response.uid, null, null, response.url, response.fee,
        response.carrier, response.status, NoError)
  }

  fun mapPayment(response: TransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(response.uid, response.hash, response.orderReference, null, null,
        null, response.status, NoError)
  }

  fun mapPaymentError(throwable: Throwable): CarrierPaymentModel {
    throwable.printStackTrace()

    val code = if (throwable is HttpException) throwable.code() else null
    val isNoNetworkException = throwable.isNoNetworkException()
    var carrierError: CarrierError = GenericError(isNoNetworkException, code, throwable.message)

    // If we retrieve a specific error from response body, specify the error
    if (throwable is HttpException) {
      throwable.response()
          ?.errorBody()
          ?.let { body ->
            val errorConverter: Converter<ResponseBody, CarrierErrorResponse> = retrofit
                .responseBodyConverter(CarrierErrorResponse::class.java,
                    arrayOfNulls<Annotation>(0))
            val bodyErrorResponse = try {
              errorConverter.convert(body)
            } catch (e: Exception) {
              e.printStackTrace()
              null
            } finally {
              body.close()
            }
            carrierError = mapErrorResponseToCarrierError(code, bodyErrorResponse)
                ?: carrierError
          }
    }

    return CarrierPaymentModel(carrierError)
  }

  private fun mapErrorResponseToCarrierError(httpCode: Int?,
                                             response: CarrierErrorResponse?): CarrierError? {
    if (response?.data == null || response.data.isEmpty()) {
      return null
    }

    val error = response.data[0]
    when (response.code) {
      "Body.Fields.Invalid" -> {
        if (error.name == "phone_number") {
          return InvalidPhoneNumber(httpCode,
              error.messages?.technical)
        }
      }
      "Resource.Gateways.Dimoco.Transactions.InvalidPrice" -> {
        val type = when (error.type) {
          "UPPER_BOUND" -> InvalidPriceError.BoundType.UPPER
          "LOWER_BOUND" -> InvalidPriceError.BoundType.LOWER
          else -> null
        }
        if (type != null && error.value != null) {
          return InvalidPriceError(httpCode, response.text, type, error.value)
        }
      }
    }
    return null
  }
}