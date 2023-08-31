package com.appcoins.wallet.ui.widgets


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

data class
GameDetailsData(
  val title: String,
  val gameIcon: String,
  val gameBackground: String?,
  val gamePackage: String,
  val description: String,
  val screenshots: List<Screenshot>,
  val rating: Double,
  val downloads: Long,
  val size: Long
)

data class Screenshot(
  val imageUrl: String,
  val height: Int,
  val width: Int
)


@Composable
fun GameDetails(
  appDetailsData: GameDetailsData,
  close: () -> Unit,
  function: () -> Unit,
) {

}

@Composable
private fun TopAppView(
  appDetailsData: GameDetailsData,
  close: () -> Unit
) {

}

@Composable
private fun BoxShadow() {

}

@Composable
private fun AppInfoRow(appInfo: GameDetailsData) {

}

@Composable
private fun AppStatsItem(
  icon: Int,
  description: String,
  text: String
) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EskillsCard() {

}

@Composable
private fun EskillsCardContent() {

}

@Composable
private fun ScreenShotsBundle(appDetailsData: GameDetailsData) {

}

@Composable
private fun ScreenshotItem(item: Screenshot) {

}

@Composable
private fun Description(appDetailsData: GameDetailsData) {

}

@Composable
fun EskillsCardList() {

}

@Composable
fun AnimatedButton() {

}