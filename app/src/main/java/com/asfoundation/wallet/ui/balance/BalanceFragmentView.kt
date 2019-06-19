package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface BalanceFragmentView {

  fun setupUI()

  fun updateTokenValue(tokenBalance: Balance)

  fun updateOverallBalance(overallBalance: FiatValue)

  fun getCreditClick(): Observable<Any>

  fun showCreditsDetails()

  fun getAppcClick(): Observable<Any>

  fun showAppcDetails()

  fun getEthClick(): Observable<Any>

  fun showEthDetails()

  fun getTopUpClick(): Observable<Any>

  fun showTopUpScreen()

}
