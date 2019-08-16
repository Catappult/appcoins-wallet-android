package com.asfoundation.wallet.poa_wallet_validation

import io.reactivex.Observable

interface ValidationSuccessView {

  fun setupUI()

  fun clean()

  fun handleAnimationEnd(): Observable<Boolean>

}