package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.R

@Composable
fun AlertMessageWithIcon(icon: Int, title: String, message: String) {
  Row {
    Icon(
      painter = painterResource(id = icon),
      contentDescription = null,
      modifier = Modifier.size(32.dp),
      tint = WalletColors.styleguide_pink
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(
        text = title,
        color = WalletColors.styleguide_white,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = message,
        color = WalletColors.styleguide_dark_grey,
        fontSize = 12.sp
      )
    }

  }
}

@Preview
@Composable
fun PreviewAlertMessageWithIcon() {
  AlertMessageWithIcon(
    icon = R.drawable.ic_alert_circle,
    title = stringResource(id = R.string.intro_backup_card_title),
    message = stringResource(id = R.string.backup_wallet_tooltip)
  )
}
