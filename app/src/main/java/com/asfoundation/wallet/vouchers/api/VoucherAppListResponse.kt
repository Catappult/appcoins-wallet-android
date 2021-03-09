package com.asfoundation.wallet.vouchers.api

data class VoucherAppListResponse(val next: String?, val previous: String?,
                                  val items: List<VoucherAppResponse>)