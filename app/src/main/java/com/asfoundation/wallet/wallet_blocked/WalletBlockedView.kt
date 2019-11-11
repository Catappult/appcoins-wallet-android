package com.asfoundation.wallet.wallet_blocked

import io.reactivex.Observable

interface WalletBlockedView {

  fun getDismissCLicks(): Observable<Any>
  fun getEmailClicks(): Observable<Any>
  fun dismiss()
  fun openEmail()

}