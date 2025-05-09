package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.AlertMessageWithIcon
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupAlertCardExpanded(onClickButton: () -> Unit, fragmentName: String, buttonsAnalytics: ButtonsAnalytics?) {
  Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
    AlertMessageWithIcon(
      icon = R.drawable.ic_alert_circle,
      title = stringResource(id = R.string.intro_backup_card_title),
      message = "${stringResource(id = R.string.backup_wallet_tooltip)} ${stringResource(id = R.string.backup_title)}",
      modifier = Modifier.weight(1f)
    )
    ButtonWithText(
      label = stringResource(R.string.action_backup_wallet),
      outlineColor = WalletColors.styleguide_white,
      labelColor = WalletColors.styleguide_white,
      onClick = onClickButton,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
  }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewBackupAlertCardExpanded() {
  BackupAlertCardExpanded(onClickButton = {}, "BackupFragment", null)
}