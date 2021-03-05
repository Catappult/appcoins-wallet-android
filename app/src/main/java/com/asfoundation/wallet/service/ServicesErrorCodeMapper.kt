package com.asfoundation.wallet.service

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.*
import com.asf.wallet.R

class ServicesErrorCodeMapper {

  fun mapError(errorType: ErrorInfo.ErrorType?): Int {
    return when (errorType) {
      BLOCKED -> R.string.purchase_error_wallet_block_code_403
      SUB_ALREADY_OWNED -> R.string.promotions_empty_poa_body //TODO
      CONFLICT -> R.string.promotions_title //TODO
      else -> R.string.unknown_error
    }
  }
}
