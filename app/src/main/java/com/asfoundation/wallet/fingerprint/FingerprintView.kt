package com.asfoundation.wallet.fingerprint

import io.reactivex.Observable

interface FingerprintView {
  fun getSwitchClick(): Observable<Boolean>
  fun setSwitchState(switchState: Boolean)
}
