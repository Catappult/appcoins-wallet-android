package com.asfoundation.wallet.service

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.*
import com.asf.wallet.R

class ServicesErrorCodeMapper {

  fun mapError(errorType: ErrorInfo.ErrorType?): Int {
    return when (errorType) {
      BLOCKED -> R.string.purchase_error_wallet_block_code_403
      SUB_ALREADY_OWNED -> R.string.purchase_error_incomplete_transaction_body
      CONFLICT -> R.string.unknown_error //TODO should we have a different message for this
      else -> R.string.unknown_error
    }
  }
}
