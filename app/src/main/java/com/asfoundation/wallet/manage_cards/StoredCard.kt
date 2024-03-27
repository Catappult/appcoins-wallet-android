package com.asfoundation.wallet.manage_cards

import androidx.annotation.DrawableRes

data class StoredCard (
  val cardLastNumbers: String,
  @DrawableRes val cardIcon: Int,
)