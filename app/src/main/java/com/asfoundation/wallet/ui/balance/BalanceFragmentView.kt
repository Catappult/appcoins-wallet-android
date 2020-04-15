package com.asfoundation.wallet.ui.balance

import android.view.View
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Observable

interface BalanceFragmentView {

  fun setupUI()

  fun updateTokenValue(tokenBalance: String,
                       fiatBalance: String,
                       tokenCurrency: WalletCurrency,
                       fiatCurrency: String)

  fun updateOverallBalance(overallBalance: String, currency: String, symbol: String)

  fun getCreditClick(): Observable<View>

  fun getAppcClick(): Observable<View>

  fun getEthClick(): Observable<View>

  fun showTokenDetails(view: View)

  fun getTopUpClick(): Observable<Any>

  fun showTopUpScreen()

}
