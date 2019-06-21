package com.asfoundation.wallet.ui.balance

import io.reactivex.Observable

interface TokenDetailsView {

  fun setupUi()

  fun getOkClick(): Observable<Any>

  fun close()

  fun getTopUpClick(): Observable<Any>

  fun showTopUp()

}
