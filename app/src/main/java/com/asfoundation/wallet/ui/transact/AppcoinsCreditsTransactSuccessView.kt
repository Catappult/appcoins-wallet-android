package com.asfoundation.wallet.ui.transact

import io.reactivex.Observable

interface AppcoinsCreditsTransactSuccessView {

  fun setup(amount: String, currency: String, toAddress: String)

  fun getOkClick(): Observable<Any>

  fun close()

}
