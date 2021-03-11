package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.util.Error

data class VoucherTransactionModel(val code: String?, val redeemUrl: String?,
                                   val error: Error = Error()) {
  constructor(error: Error) : this(null, null, error)
}