package com.asfoundation.wallet.ui.balance

import android.view.View
import android.widget.ImageView
import android.widget.TextView

interface BalanceActivityView {

  fun showBalanceScreen()

  fun showTokenDetailsScreen(
      tokenDetailsId: TokenDetailsActivity.TokenDetailsId, imgView: ImageView,
      textView: TextView, parentView: View)

  fun showTopUpScreen()

  fun setupToolbar()

}
