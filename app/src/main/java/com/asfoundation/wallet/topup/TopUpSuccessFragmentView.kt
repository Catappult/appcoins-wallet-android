package com.asfoundation.wallet.topup

import io.reactivex.Observable

interface TopUpSuccessFragmentView {

  fun show()

  fun clean()

  fun close()

  fun getOKClicks(): Observable<Any>
}
