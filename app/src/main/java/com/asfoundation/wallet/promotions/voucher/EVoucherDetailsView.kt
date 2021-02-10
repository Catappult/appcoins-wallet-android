package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Observable

interface EVoucherDetailsView {

  fun setupUi(title: String)

  fun onNextClicks(): Observable<Any>

  fun onCancelClicks(): Observable<Any>
}