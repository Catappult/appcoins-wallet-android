package com.asfoundation.wallet.ui.transact

import io.reactivex.Observable
import java.math.BigDecimal

interface TransactFragmentView {
  fun getSendClick(): Observable<TransactData>

  data class TransactData(val walletAddress: String, val currency: Currency, val amount: BigDecimal)

  enum class Currency {
    APPC_C, APPC, ETH
  }
}
