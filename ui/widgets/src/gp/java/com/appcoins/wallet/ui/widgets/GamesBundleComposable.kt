package com.appcoins.wallet.ui.widgets

import androidx.compose.runtime.Composable

var gameClicked: String = ""

@Composable
fun GamesBundle(
  items: List<GameData>,
  dialog: () -> Unit,
  fetchFromApiCallback: () -> Unit
) {

}

data class GameData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String,
)
