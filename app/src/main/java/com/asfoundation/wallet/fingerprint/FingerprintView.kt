package com.asfoundation.wallet.fingerprint

import io.reactivex.Observable

interface FingerprintView {
  fun showClickText()
  fun getImageClick(): Observable<Any>
  fun getSwitchClick(): Observable<Boolean>
  fun setSwitchState(switchState: Boolean)
}
