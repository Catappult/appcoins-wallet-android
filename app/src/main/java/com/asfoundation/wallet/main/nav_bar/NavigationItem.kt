package com.asfoundation.wallet.main.nav_bar

import com.asfoundation.wallet.ui.bottom_navigation.CurrencyDestinations
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations

data class NavigationItem(
  val destination: Destinations,
  val icon: Int,
  val label: Int,
  val selected: Boolean
)

data class TransferNavigationItem(
  val destination: TransferDestinations,
  val label: Int,
  val selected: Boolean
)

data class CurrencyNavigationItem(
  val destination: CurrencyDestinations,
  val label: Int,
  val selected: Boolean
)