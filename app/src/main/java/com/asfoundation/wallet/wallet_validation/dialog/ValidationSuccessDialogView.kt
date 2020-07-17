package com.asfoundation.wallet.wallet_validation.dialog

import io.reactivex.Observable

interface ValidationSuccessDialogView {

  fun setupUI()

  fun clean()

  fun handleAnimationEnd(): Observable<Boolean>

}