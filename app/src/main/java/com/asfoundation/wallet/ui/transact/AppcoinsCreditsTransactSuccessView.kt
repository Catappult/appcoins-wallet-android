package com.asfoundation.wallet.ui.transact

import io.reactivex.Observable
import java.math.BigDecimal

interface AppcoinsCreditsTransactSuccessView {
  fun setup(amount: BigDecimal, currency: String, toAddress: String)
  fun getOkClick(): Observable<Any>
  fun close()

}
