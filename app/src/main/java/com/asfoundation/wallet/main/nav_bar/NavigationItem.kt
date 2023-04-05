package com.asfoundation.wallet.main.nav_bar

import com.asfoundation.wallet.ui.bottom_navigation.Destinations

data class NavigationItem(
  val destination: Destinations,
  val icon: Int,
  val label: Int,
  val selected: Boolean
)