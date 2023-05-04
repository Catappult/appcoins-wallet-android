package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.AlertMessageWithIcon
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupAlertCardExpanded(onClickButton: () -> Unit) {
  Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
    AlertMessageWithIcon(
      icon = R.drawable.ic_alert_circle,
      title = stringResource(id = R.string.intro_backup_card_title),
      message = "${stringResource(id = R.string.backup_wallet_tooltip)} ${stringResource(id = R.string.backup_title)}"
    )
    ButtonWithText(
      label = R.string.action_backup_wallet,
      outlineColor = WalletColors.styleguide_white,
      labelColor = WalletColors.styleguide_white,
      onClick = onClickButton
    )
  }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewBackupAlertCardExpanded() {
  BackupAlertCardExpanded(onClickButton = {})
}