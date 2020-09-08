package com.asfoundation.wallet.service

import com.asf.wallet.R

class ServicesErrorCodeMapper : ServicesErrorMapper {

  companion object {
    const val FORBIDDEN = 403
  }

  override fun mapError(errorCode: Int): Int {
    return when (errorCode) {
      FORBIDDEN -> R.string.purchase_error_wallet_block_code_403
      else -> R.string.unknown_error
    }
  }
}
