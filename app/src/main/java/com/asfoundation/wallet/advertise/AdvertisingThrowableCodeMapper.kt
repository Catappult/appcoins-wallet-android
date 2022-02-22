package com.asfoundation.wallet.advertise

import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

class AdvertisingThrowableCodeMapper @Inject constructor() {
  internal fun map(throwable: Throwable): Advertising.CampaignAvailabilityType {
    return when (throwable) {
      is HttpException -> {
        mapHttpCode(throwable)
      }
      is UnknownHostException -> {
        Advertising.CampaignAvailabilityType.NO_INTERNET_CONNECTION
      }
      else -> {
        throwable.printStackTrace()
        Advertising.CampaignAvailabilityType.UNKNOWN_ERROR
      }
    }
  }

  private fun mapHttpCode(throwable: HttpException): Advertising.CampaignAvailabilityType =
      when (throwable.code()) {
        404 -> Advertising.CampaignAvailabilityType.PACKAGE_NAME_NOT_FOUND
        in 500..599 -> Advertising.CampaignAvailabilityType.API_ERROR
        else -> {
          throwable.printStackTrace()
          Advertising.CampaignAvailabilityType.UNKNOWN_ERROR
        }
      }
}
