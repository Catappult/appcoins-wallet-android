package com.asfoundation.wallet.wallet_validation

import io.reactivex.subjects.Subject

interface ValidationSuccessView {

  fun setupUI()

  fun clean()

  fun handleAnimationEnd(): Subject<Boolean>

}