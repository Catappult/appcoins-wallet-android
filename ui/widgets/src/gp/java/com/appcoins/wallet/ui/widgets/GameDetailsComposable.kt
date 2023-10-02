package com.appcoins.wallet.ui.widgets


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
  val size: Long,
  val md5: String,
  val url: String,
  val version: Int
)

data class Screenshot(
  val imageUrl: String,
  val height: Int,
  val width: Int
)

val grantedPermission = mutableStateOf(false)

var showEskillsCard by mutableStateOf(true)
var showInstallButton by mutableStateOf(true)
var showResume by mutableStateOf(false)


@Composable
fun GameDetails(
  appDetailsData: GameDetailsData,
  progress: Int,
  close: () -> Unit,
  install: () -> Unit,
  isAppInstalled: () -> Boolean,
  cancel: () -> Unit,
  pause: () -> Unit,
  finishedInstall: Boolean,
  installing: Boolean,
  open: () -> Unit,
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