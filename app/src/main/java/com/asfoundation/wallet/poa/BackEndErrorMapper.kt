package com.asfoundation.wallet.poa

import com.asfoundation.wallet.entity.SubmitPoAException
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.ALREADY_SUBMITTED_FOR_IP
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.ALREADY_SUBMITTED_FOR_WALLET
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.CAMPAIGN_NOT_AVAILABLE
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.CAMPAIGN_NOT_EXISTENT
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.INCORRECT_DATA
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.NOT_AVAILABLE_FOR_COUNTRY
import com.asfoundation.wallet.entity.SubmitPoAException.Companion.NOT_ENOUGH_BUDGET
import com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.*
import retrofit2.HttpException
import java.net.UnknownHostException

class BackEndErrorMapper {

  fun map(throwable: Throwable): BackEndError {
    if (throwable is UnknownHostException) {
      return NO_INTERNET
    }
    if (throwable is SubmitPoAException) {
      when (throwable.error) {
        CAMPAIGN_NOT_EXISTENT,
        CAMPAIGN_NOT_AVAILABLE,
        NOT_ENOUGH_BUDGET -> return BACKEND_CAMPAIGN_NOT_AVAILABLE
        NOT_AVAILABLE_FOR_COUNTRY -> return BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY
        ALREADY_SUBMITTED_FOR_IP,
        ALREADY_SUBMITTED_FOR_WALLET -> return BACKEND_ALREADY_AWARDED
        INCORRECT_DATA -> return BACKEND_INVALID_DATA
      }
    }
    if (throwable is HttpException) {
      when (throwable.code()) {
        401 -> return BACKEND_PHONE_NOT_VERIFIED
      }
    }
    return BACKEND_GENERIC_ERROR
  }

  enum class BackEndError {
    BACKEND_GENERIC_ERROR,
    BACKEND_CAMPAIGN_NOT_AVAILABLE,
    BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY,
    BACKEND_ALREADY_AWARDED,
    BACKEND_INVALID_DATA,
    BACKEND_PHONE_NOT_VERIFIED,
    NO_INTERNET
  }
}
