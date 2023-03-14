package com.appcoins.wallet.ui.widgets

import android.view.View
import android.view.ViewGroup
import com.asfoundation.wallet.util.convertDpToPx

fun View.setMargins(startMarginDp: Int? = null,
                    topMarginDp: Int? = null,
                    endMarginDp: Int? = null,
                    bottomMarginDp: Int? = null) {
  if (layoutParams is ViewGroup.MarginLayoutParams) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    startMarginDp?.run { params.marginStart = this.convertDpToPx(context.resources) }
    topMarginDp?.run { params.topMargin = this.convertDpToPx(context.resources) }
    endMarginDp?.run { params.marginEnd = this.convertDpToPx(context.resources) }
    bottomMarginDp?.run { params.bottomMargin = this.convertDpToPx(context.resources) }
    requestLayout()
  }
}