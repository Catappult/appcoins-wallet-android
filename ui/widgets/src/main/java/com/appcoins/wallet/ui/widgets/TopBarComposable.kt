package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun TopBar(
  isMainBar: Boolean,
  onClickNotifications: () -> Unit = {},
  onClickSettings: () -> Unit = {},
  onClickSupport: () -> Unit = {},
  onClickBack: (() -> Unit)? = null,
  hasNotificationBadge: Boolean = false,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(WalletColors.styleguide_blue)
      .height(64.dp)
      .padding(start = 16.dp, end = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    if (isMainBar) {
      WalletLogo()
    } else {
      BackButton(onClickBack, fragmentName, buttonsAnalytics)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (isMainBar) {
        SettingsButton(onClickSettings, fragmentName, buttonsAnalytics)
      }
      SupportButton(onClickSupport, hasNotificationBadge, fragmentName, buttonsAnalytics)
    }
  }
}

// This TopBar was created because there is a bug on compose when that is used in a screen with
// TextField
@Composable
fun TopBar(
  onClickSupport: () -> Unit = {},
  onClickBack: (() -> Unit)? = null,
  hasNotificationBadge: Boolean = false,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Row(
    modifier =
    Modifier
      .fillMaxWidth()
      .background(WalletColors.styleguide_blue)
      .padding(start = 16.dp, end = 4.dp)
      .height(64.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    BackButton(onClickBack, fragmentName, buttonsAnalytics)
    SupportButton(onClickSupport, hasNotificationBadge, fragmentName, buttonsAnalytics)
  }
}

@Composable
fun WalletLogo() {
  Image(
    painter = painterResource(R.drawable.ic_app_logo),
    null,
    modifier = Modifier.heightIn(max = 24.dp)
  )
}

@Composable
fun NotificationsButton(
  onClickNotifications: () -> Unit = {},
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  ActionButton(
    imagePainter = painterResource(R.drawable.ic_notifications),
    description = "Notifications",
    onClick = onClickNotifications,
    hasRedBadge = false,
    fragmentName = fragmentName,
    buttonsAnalytics = buttonsAnalytics
  )
}

@Composable
fun SettingsButton(
  onClickSettings: () -> Unit = {},
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  ActionButton(
    imagePainter = painterResource(R.drawable.ic_settings_white_24dp),
    description = "Settings",
    onClick = onClickSettings,
    hasRedBadge = false,
    fragmentName = fragmentName,
    buttonsAnalytics = buttonsAnalytics
  )
}

@Composable
fun SupportButton(
  onClickSupport: () -> Unit = {},
  hasNotificationBadge: Boolean,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  ActionButton(
    imagePainter = painterResource(R.drawable.ic_settings_support),
    description = "Support",
    onClick = onClickSupport,
    hasRedBadge = hasNotificationBadge,
    fragmentName = fragmentName,
    buttonsAnalytics = buttonsAnalytics
  )
}

@Composable
fun BackButton(
  onClickBack: (() -> Unit)? = null,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  if (onClickBack != null)
    ActionButton(
      imagePainter = painterResource(R.drawable.ic_arrow_back),
      description = "Back",
      onClick = onClickBack,
      hasRedBadge = false,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
  else {
    val activity = LocalContext.current.getActivity()
    if (activity != null)
      ActionButton(
        imagePainter = painterResource(R.drawable.ic_arrow_back),
        description = "Back",
        onClick = { activity.onBackPressed() },
        hasRedBadge = false,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
  }
}

@Composable
fun ScreenTitle(title: String) {
  Text(
    text = title,
    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    style = MaterialTheme.typography.headlineSmall,
    fontWeight = FontWeight.Bold,
    color = WalletColors.styleguide_light_grey,
  )
}

@Preview
@Composable
fun TopBarPreview() {
  TopBar(
    isMainBar = true,
    hasNotificationBadge = true,
    fragmentName = "TestFragment",
    buttonsAnalytics = null
  )
}
