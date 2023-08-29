package com.appcoins.wallet.feature.backup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupDialogCardAlertBottomSheet(onCancelClick: () -> Unit, onConfirmClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(32.dp)
  ) {
    Image(
      modifier = Modifier.size(72.dp),
      painter = painterResource(id = R.drawable.ic_alert_circle),
      contentDescription = null
    )

    Text(
      text = stringResource(id = R.string.backup_skip_title),
      style = WalletTypography.medium.sp16,
      color = WalletColors.styleguide_light_grey,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 24.dp)
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 40.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(Modifier.fillMaxWidth(0.49f)) {
        ButtonWithText(
          label = stringResource(id = R.string.cancel_button),
          onClick = { onCancelClick() },
          backgroundColor = Color.Transparent,
          labelColor = WalletColors.styleguide_white,
          outlineColor = WalletColors.styleguide_white,
          buttonType = ButtonType.LARGE
        )
      }
      ButtonWithText(
        label = stringResource(id = R.string.confirm_button),
        onClick = { onConfirmClick() },
        backgroundColor = WalletColors.styleguide_pink,
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.LARGE
      )
    }
  }
}

@Preview
@Composable
fun PreviewBackupDialogCardAlertBottomSheet() {
  BackupDialogCardAlertBottomSheet({}, {})
}
