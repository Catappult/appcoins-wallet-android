package com.asfoundation.wallet.withdraw.ui

import io.reactivex.Observable
import java.math.BigDecimal

interface WithdrawView {
  fun getWithdrawClicks(): Observable<Pair<String, BigDecimal>>
  fun showError(error: Throwable)
  fun showWithdrawSuccessMessage()
  fun showNotEnoughBalanceError()
  fun showNotEnoughEarningsBalanceError()
  fun showNoNetworkError()
  fun showLoading()
  fun hideLoading()
  fun showInvalidEmailError()

}
