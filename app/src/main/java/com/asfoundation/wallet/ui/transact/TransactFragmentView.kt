package com.asfoundation.wallet.ui.transact

import io.reactivex.Observable

interface TransactFragmentView {
  fun getSendClick(): Observable<Any>
}
