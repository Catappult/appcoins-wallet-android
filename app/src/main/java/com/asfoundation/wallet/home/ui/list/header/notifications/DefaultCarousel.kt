package com.asfoundation.wallet.home.ui.list.header.notifications

import android.content.Context
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView
import com.appcoins.wallet.ui.common.convertDpToPx

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DefaultCarousel(context: Context) : Carousel(context) {

  override fun getPaddingBottom(): Int {
    return 0
  }

  override fun getPaddingTop(): Int {
    return 0
  }

  override fun getPaddingEnd(): Int {
    return 8.convertDpToPx(context.resources)
  }

  override fun getPaddingStart(): Int {
    return 8.convertDpToPx(context.resources)
  }
}