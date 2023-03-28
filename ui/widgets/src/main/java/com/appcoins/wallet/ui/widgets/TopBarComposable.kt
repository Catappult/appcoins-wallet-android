package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
  isMainBar: Boolean,
  isVip: Boolean = false,
  onClickNotifications: () -> Unit = {},
  onClickSettings: () -> Unit = {},
  onClickSupport: () -> Unit = {},
  onClickBack: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .background(colorResource(R.color.styleguide_blue))
      .fillMaxWidth()
      .height(64.dp)
      .padding(start = 16.dp, end = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ){
    if (isMainBar) {
      Image(
        painter = painterResource(R.drawable.ic_app_logo),
        "Wallet",
        modifier = Modifier
          .heightIn(max = 24.dp)
      )
    } else {
      ActionButton(
        imagePainter = painterResource(R.drawable.ic_back_button),  //TODO icon
        description = "Back",
        onClick = onClickBack
      )
    }

    Spacer(Modifier.weight(1f))

    if (isMainBar) {
      if (isVip) {
        VipBadge()
      }
      ActionButton(
        imagePainter = painterResource(R.drawable.baseline_notification),  //TODO icon
        description = "Notifications",
        onClick = onClickNotifications
      )
      ActionButton(
        imagePainter = painterResource(R.drawable.ic_settings_white_24dp),
        description = "Settings",
        onClick = onClickSettings
      )
    }
    ActionButton(
      imagePainter = painterResource(R.drawable.ic_settings_support),
      description = "Support",
      onClick = onClickSupport
    )
  }
}

@Composable
fun VipBadge() {
  Row(
    modifier = Modifier
      .height(32.dp)
      .width(65.dp)
      .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
      .background(colorResource(R.color.styleguide_vip_yellow_transparent_40))
      .padding(start = 6.dp, end = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      painter = painterResource(R.drawable.ic_vip_badge),
      contentDescription = "Vip",
      modifier = Modifier
        .padding(top = 2.dp)
    )
    Spacer(Modifier.weight(1f))
    Text (
      stringResource(R.string.vip),
      color = colorResource(id = R.color.styleguide_white)
    )
  }
}
