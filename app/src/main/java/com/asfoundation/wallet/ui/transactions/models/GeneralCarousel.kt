package com.asfoundation.wallet.ui.transactions.models

import android.content.Context
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class GeneralCarousel(context: Context) : Carousel(context) {

  override fun getSnapHelperFactory(): SnapHelperFactory? {
    return null
  }
}