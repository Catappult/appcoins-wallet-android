package com.asfoundation.wallet.wallet_validation.poa

import io.reactivex.Observable

interface PoaValidationSuccessView {

  fun setupUI()

  fun clean()

  fun handleAnimationEnd(): Observable<Boolean>

}