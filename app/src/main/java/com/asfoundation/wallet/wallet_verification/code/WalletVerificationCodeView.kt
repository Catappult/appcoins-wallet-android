package com.asfoundation.wallet.wallet_verification.code

import io.reactivex.Observable

interface WalletVerificationCodeView {

  fun showLoading()

  fun hideLoading()

  fun showSuccess()

  fun showGenericError()

  fun showNetworkError()

  fun showSpecificError(stringRes: Int)

  fun updateUi()

  fun getMaybeLaterClicks(): Observable<Any>

  fun getConfirmClicks(): Observable<String>

}