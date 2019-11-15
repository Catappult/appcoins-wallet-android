package com.asfoundation.wallet.ui.balance

import android.view.View
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface BalanceFragmentView {

  fun setupUI()

  fun updateTokenValue(tokenBalance: TokenBalance)

  fun updateOverallBalance(overallBalance: FiatValue)

  fun getCreditClick(): Observable<View>

  fun getAppcClick(): Observable<View>

  fun getEthClick(): Observable<View>

  fun showTokenDetails(view: View)

  fun getTopUpClick(): Observable<Any>

  fun showTopUpScreen()

}
