package com.asfoundation.wallet.service

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.*
import com.asf.wallet.R
import javax.inject.Inject

class ServicesErrorCodeMapper @Inject constructor() {

  fun mapError(errorType: ErrorInfo.ErrorType?): Int {
    return when (errorType) {
      BLOCKED -> R.string.purchase_error_wallet_block_code_403
      SUB_ALREADY_OWNED -> R.string.subscriptions_error_already_subscribed
      CONFLICT -> R.string.unknown_error //TODO should we have a different message for this
      else -> R.string.unknown_error
    }
  }
}
