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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun TopBar(
  isMainBar: Boolean,
  isVip: Boolean = false,
  onClickNotifications: () -> Unit = {},
  onClickSettings: () -> Unit = {},
  onClickSupport: () -> Unit = {},
  onClickBack: () -> Unit = {}
) {
  TopAppBar(
    modifier = Modifier
      .height(64.dp),
    backgroundColor = WalletColors.styleguide_blue,
    elevation = 4.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isMainBar) {
        Image(
          painter = painterResource(R.drawable.ic_app_logo),
          "Wallet",
          modifier = Modifier
            .heightIn(max = 24.dp)
        )
      } else {
        ActionButton(
          imagePainter = painterResource(R.drawable.ic_arrow_back),
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
          imagePainter = painterResource(R.drawable.ic_notifications),
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
}

@Composable
fun VipBadge() {
  Row(
    modifier = Modifier
      .height(32.dp)
      .width(65.dp)
      .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
      .background(WalletColors.styleguide_vip_yellow_transparent_40)
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
    Text(
      stringResource(R.string.vip),
      color = WalletColors.styleguide_white
    )
  }
}

@Preview
@Composable
fun TopBarPreview() {
  TopBar(
    isMainBar = true,
    isVip = true,
  )
}
