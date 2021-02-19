package com.asfoundation.wallet.ui.iab.vouchers

import io.reactivex.Observable

interface VouchersSuccessView {

  fun setupUi(bonus: String, code: String, redeem: String)

  fun getGotItClick(): Observable<Any>
}
