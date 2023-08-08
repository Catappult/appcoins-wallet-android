package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.appcoins.wallet.ui.common.theme.WalletColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
  isMainBar: Boolean,
  isVip: Boolean = false,
  onClickNotifications: () -> Unit = {},
  onClickSettings: () -> Unit = {},
  onClickSupport: () -> Unit = {},
  onClickBack: (() -> Unit)? = null
) {
  TopAppBar(
    modifier = Modifier
      .fillMaxWidth(),
    colors = TopAppBarDefaults.mediumTopAppBarColors(WalletColors.styleguide_blue),
    title = { },
    navigationIcon = {
      Row(
        modifier = Modifier
          .padding(start = 16.dp)
          .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isMainBar) {
          Image(
            painter = painterResource(R.drawable.ic_app_logo),
            null,
            modifier = Modifier.heightIn(max = 24.dp)
          )
        } else {
          if (onClickBack != null)
            ActionButton(
              imagePainter = painterResource(R.drawable.ic_arrow_back),
              description = "Back",
              onClick = onClickBack
            )
          else {
            val activity = LocalContext.current.getActivity()
            if (activity != null)
              ActionButton(
                imagePainter = painterResource(R.drawable.ic_arrow_back),
                description = "Back",
                onClick = { activity.onBackPressed() })
          }
        }
      }
    },
    actions = {
      Row(
        modifier = Modifier
          .padding(end = 4.dp)
          .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isMainBar) {
          if (isVip) {
            VipBadge()
          }
//          ActionButton(
//            imagePainter = painterResource(R.drawable.ic_notifications),
//            description = "Notifications",
//            onClick = onClickNotifications
//          )
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
    })
}

@Composable
fun VipBadge() {
  Row(
    modifier = Modifier
      .size(width = 64.dp, height = 32.dp)
      .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
      .background(WalletColors.styleguide_vip_yellow_transparent_40)
      .padding(start = 4.dp, end = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Image(
      painter = painterResource(R.drawable.ic_vip_badge),
      contentDescription = stringResource(R.string.vip),
      modifier = Modifier
        .padding(top = 2.dp)
    )
    Text(stringResource(R.string.vip), color = WalletColors.styleguide_white)
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
