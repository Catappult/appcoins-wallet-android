package com.asfoundation.wallet.manage_cards.models

import androidx.annotation.DrawableRes

data class StoredCard (
  val cardLastNumbers: String,
  @DrawableRes val cardIcon: Int,
  var recurringReference: String?
)