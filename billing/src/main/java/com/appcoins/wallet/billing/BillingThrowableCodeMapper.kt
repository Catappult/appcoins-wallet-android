package com.appcoins.wallet.billing

import retrofit2.HttpException

class BillingThrowableCodeMapper {
  internal fun map(throwable: Throwable): Billing.BillingSupportType {
    return when (throwable) {
      is HttpException -> {
        mapHttpCode(throwable)
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
        else -> {
          throwable.printStackTrace()
          Billing.BillingSupportType.UNKNOWN_ERROR
        }
      }


}
