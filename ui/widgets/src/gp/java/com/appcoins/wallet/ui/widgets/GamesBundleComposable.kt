package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

@Composable
fun GamesBundle(
  stateList: LazyListState,
  items: List<GameData>,
  sendPromotionClickEvent: (String?, String) -> Unit,
  fetchFromApiCallback: () -> Unit
) {

}

data class GameData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String,
  val gamePackage: String,
  val actionUrl: String?,
)
