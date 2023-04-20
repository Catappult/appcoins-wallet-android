package com.appcoins.wallet.ui.widgets

import androidx.compose.runtime.Composable

@Composable
fun GamesBundle(
  items: List<GameCardData>,
  fetchFromApiCallback: () -> Unit
) {

}

data class GameCardData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String,
  val onClick: () -> Unit
)
