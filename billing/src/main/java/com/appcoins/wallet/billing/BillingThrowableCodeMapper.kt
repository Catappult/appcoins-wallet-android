package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.exceptions.ApiException
import com.appcoins.wallet.billing.exceptions.ServiceUnavailableException
import com.appcoins.wallet.billing.exceptions.UnknownException
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

class BillingThrowableCodeMapper {
  internal fun map(throwable: Throwable): Billing.BillingSupportType {
    return when (throwable) {
      is HttpException -> {
        mapHttpCode(throwable)
      }
      is UnknownHostException -> {
        Billing.BillingSupportType.NO_INTERNET_CONNECTION
      }
      else -> {
        throwable.printStackTrace()
        Billing.BillingSupportType.UNKNOWN_ERROR
      }
    }
  }

  private fun mapHttpCode(throwable: HttpException): Billing.BillingSupportType =
      when (throwable.code()) {
        404 -> Billing.BillingSupportType.MERCHANT_NOT_FOUND
        in 500..599 -> Billing.BillingSupportType.API_ERROR
        else -> {
          throwable.printStackTrace()
          Billing.BillingSupportType.UNKNOWN_ERROR
        }
      }

  fun mapException(throwable: Throwable): Exception {
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is IOException -> ServiceUnavailableException(
          AppcoinsBillingBinder.RESULT_SERVICE_UNAVAILABLE)
      else -> UnknownException(AppcoinsBillingBinder.RESULT_ERROR)
    }
  }

  private fun mapHttpException(throwable: HttpException): Exception {
    return when (throwable.code()) {
      in 500..599 -> ApiException(
          AppcoinsBillingBinder.RESULT_ERROR)
      else -> ApiException(
          AppcoinsBillingBinder.RESULT_ERROR)
    }
  }


}
