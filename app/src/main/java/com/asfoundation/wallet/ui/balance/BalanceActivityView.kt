package com.asfoundation.wallet.ui.balance

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.Observable

interface BalanceActivityView {

  fun showBalanceScreen()

  fun showTokenDetailsScreen(
      tokenDetailsId: TokenDetailsActivity.TokenDetailsId, imgView: ImageView,
      textView: TextView, parentView: View)

  fun showTopUpScreen()

  fun setupToolbar()

  fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean)

  fun shouldExpandBottomSheet(): Boolean

  fun enableBack()

  fun disableBack()

  fun backPressed(): Observable<Any>
}
