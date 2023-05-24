package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.AlertMessageWithIcon
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupAlertCard(onClickButton: () -> Unit) {
  AlertCard(
    onClickButton = onClickButton,
    title = R.string.intro_backup_card_title,
    message = R.string.backup_wallet_tooltip,
    buttonLabel = R.string.action_backup_wallet
  )
}

@Composable
fun VerifyWalletAlertCard(onClickButton: () -> Unit) {
  AlertCard(
    onClickButton = onClickButton,
    title = R.string.referral_verification_title,
    message = R.string.mywallet_unverified_body,
    buttonLabel = R.string.referral_verification_title
  )
}

@Composable
fun AlertCard(onClickButton: () -> Unit, title: Int, message: Int, buttonLabel: Int) {
  Column {
    AlertMessageWithIcon(
      icon = R.drawable.ic_alert_circle,
      title = stringResource(id = title),
      message = "${stringResource(id = message)} ${stringResource(id = R.string.backup_title)}"
    )
    Spacer(modifier = Modifier.height(16.dp))
    ButtonWithText(
      label = stringResource(buttonLabel),
      outlineColor = WalletColors.styleguide_white,
      labelColor = WalletColors.styleguide_white,
      onClick = onClickButton,
      buttonType = ButtonType.LARGE
    )
  }
}

@Preview
@Composable
fun PreviewBackupAlertCard() {
  BackupAlertCard(onClickButton = {})
}

@Preview
@Composable
fun PreviewVerifyAlertCard() {
  VerifyWalletAlertCard(onClickButton = {})
}