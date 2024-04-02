package com.appcoins.wallet.ui.widgets

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun BackupAlertCard(
  modifier: Modifier = Modifier,
  onClickButton: () -> Unit,
  hasBackup: Boolean,
  backupDate: Long = 0L,
) {
  AlertCard(
    modifier = modifier,
    onClickPositiveButton = onClickButton,
    title = if (hasBackup) R.string.backup_confirmation_no_share_title else R.string.my_wallets_action_backup_wallet,
    message = if (hasBackup)
      stringResource(
        id = R.string.mywallet_backed_up_date,
        DateFormat.format("dd/MM/yyyy", Date(backupDate)).toString()
      )
    else stringResource(R.string.backup_wallet_tooltip),
    positiveButtonLabel = stringResource(id = if (hasBackup) R.string.mywallet_backup_again_button else R.string.action_backup_wallet),
    icon = if (hasBackup) R.drawable.ic_check_circle else R.drawable.ic_alert_circle
  )
}

@Composable
fun VerifyWalletAlertCard(
  onClickButton: () -> Unit,
  verified: Boolean,
  waitingCode: Boolean,
  onCancelClickButton: () -> Unit,
  modifier: Modifier = Modifier
) {
  AlertCard(
    onClickPositiveButton = onClickButton,
    title = verifyCardTitle(verified, waitingCode),
    message = stringResource(id = verifyCardMessage(waitingCode)),
    positiveButtonLabel = stringResource(id = verifyCardPositiveButtonLabel(waitingCode)),
    icon = verifyCardIcon(verified),
    onClickNegativeButton = onCancelClickButton,
    negativeButtonLabel = verifyCardNegativeButtonLabel(waitingCode),
    modifier = modifier
  )
}

@Composable
fun AlertCard(
  modifier: Modifier = Modifier,
  onClickPositiveButton: () -> Unit,
  title: Int,
  message: String,
  positiveButtonLabel: String,
  icon: Int,
  onClickNegativeButton: () -> Unit = {},
  negativeButtonLabel: String = "",
) {
  Column(modifier = modifier) {
    AlertMessageWithIcon(
      icon = icon,
      title = stringResource(id = title),
      message = message
    )
    Spacer(modifier = Modifier.height(16.dp))
    CardButtons(
      onClickPositiveButton = onClickPositiveButton,
      positiveButtonLabel = positiveButtonLabel,
      onClickNegativeButton = onClickNegativeButton,
      negativeButtonLabel = negativeButtonLabel,
    )
  }
}

@Composable
fun CardButtons(
  onClickPositiveButton: () -> Unit,
  positiveButtonLabel: String,
  onClickNegativeButton: () -> Unit = {},
  negativeButtonLabel: String = "",
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    if (negativeButtonLabel.isNotEmpty())
      ButtonWithText(
        modifier = Modifier.weight(1f),
        label = negativeButtonLabel,
        labelColor = WalletColors.styleguide_white,
        onClick = onClickNegativeButton,
        buttonType = ButtonType.LARGE
      )
    ButtonWithText(
      modifier = Modifier.weight(1f),
      label = positiveButtonLabel,
      outlineColor = WalletColors.styleguide_white,
      labelColor = WalletColors.styleguide_white,
      onClick = onClickPositiveButton,
      buttonType = ButtonType.LARGE
    )
  }
}

@Preview
@Composable
fun PreviewCardButtons() {
  CardButtons(
    onClickPositiveButton = {},
    positiveButtonLabel = stringResource(R.string.card_verification_wallets_insert_bode_button),
    onClickNegativeButton = {},
  )
}

@Preview
@Composable
fun PreviewBackupAlertCard() {
  BackupAlertCard(onClickButton = {}, hasBackup = true)
}

@Preview
@Composable
fun PreviewVerifyAlertCard() {
  VerifyWalletAlertCard(onClickButton = {}, verified = false, waitingCode = true, {})
}

private fun verifyCardTitle(verified: Boolean, waitingCode: Boolean): Int {
  return if (verified) R.string.verification_settings_verified_title
  else if (waitingCode) R.string.paypal_verification_home_one_step_card_title
  else R.string.referral_verification_title
}

private fun verifyCardMessage(waitingCode: Boolean) =
  if (waitingCode) R.string.paypal_verification_home_one_step_card_body else R.string.mywallet_unverified_body

private fun verifyCardPositiveButtonLabel(waitingCode: Boolean): Int =
  if (waitingCode) R.string.card_verification_wallets_insert_bode_button else R.string.referral_verification_title

private fun verifyCardIcon(verified: Boolean) =
  if (verified) R.drawable.ic_check_circle else R.drawable.ic_alert_circle

@Composable
private fun verifyCardNegativeButtonLabel(waitingCode: Boolean) =
  if (waitingCode) stringResource(id = R.string.cancel_button) else ""
