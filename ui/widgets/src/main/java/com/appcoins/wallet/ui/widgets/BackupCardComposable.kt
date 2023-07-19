package com.appcoins.wallet.ui.widgets

import android.text.format.DateFormat
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
import java.util.Date

@Composable
fun BackupAlertCard(onClickButton: () -> Unit, hasBackup: Boolean, backupDate: Long = 0L) {
  AlertCard(
    onClickButton = onClickButton,
    title = if (hasBackup) R.string.backup_confirmation_no_share_title else R.string.my_wallets_action_backup_wallet,
    message = if (hasBackup)
      stringResource(
        id = R.string.mywallet_backed_up_date,
        DateFormat.format("dd/MM/yyyy", Date(backupDate)).toString()
      )
    else stringResource(R.string.backup_wallet_tooltip),
    buttonLabel = if (hasBackup) R.string.mywallet_backup_again_button else R.string.action_backup_wallet,
    icon = if (hasBackup) R.drawable.ic_check_circle else R.drawable.ic_alert_circle
  )
}

@Composable
fun VerifyWalletAlertCard(onClickButton: () -> Unit, verified: Boolean) {
  AlertCard(
    onClickButton = onClickButton,
    title = if (verified) R.string.verification_settings_verified_title else R.string.referral_verification_title,
    message = stringResource(R.string.mywallet_unverified_body),
    buttonLabel = R.string.referral_verification_title,
    icon = if (verified) R.drawable.ic_check_circle else R.drawable.ic_alert_circle
  )
}

@Composable
fun AlertCard(onClickButton: () -> Unit, title: Int, message: String, buttonLabel: Int, icon: Int) {
  Column {
    AlertMessageWithIcon(
      icon = icon,
      title = stringResource(id = title),
      message = message
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
  BackupAlertCard(onClickButton = {}, true)
}

@Preview
@Composable
fun PreviewVerifyAlertCard() {
  VerifyWalletAlertCard(onClickButton = {}, false)
}