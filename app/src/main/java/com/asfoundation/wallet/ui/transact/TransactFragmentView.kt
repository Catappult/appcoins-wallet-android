package com.asfoundation.wallet.ui.transact

import io.reactivex.Observable

interface TransactFragmentView {
  fun getSendClick(): Observable<PaymentData>

  data class PaymentData(val walletAddress: String, val currency: Currency, val amount: Int)

  enum class Currency {
    APPC_C, APPC, ETH
  }
}
