package com.appcoins.wallet.ui.widgets

import androidx.compose.runtime.Composable
import com.appcoins.wallet.core.analytics.analytics.legacy.GetAppAnalytics

@Composable
fun GamesBundle(
  items: List<GameData>,
  analytics: GetAppAnalytics,
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
