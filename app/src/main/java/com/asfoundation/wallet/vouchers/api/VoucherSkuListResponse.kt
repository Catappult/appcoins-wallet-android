package com.asfoundation.wallet.vouchers.api

data class VoucherSkuListResponse(val next: String?, val previous: String?,
                                  val items: List<VoucherSkuResponse>)