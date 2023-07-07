package com.appcoins.wallet.feature.backup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupDialogCardAlertBottomSheet(
  onExitClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color(0x242333))
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(40.dp)
    ) {
      WalletImage(
        Modifier
          .size(68.57.dp),
        data = R.drawable.ic_alert_circle_red
      )

      Text(
        text = "I understand that I may loose my funds if I don’t backup my wallet key", //criar string
        style = WalletTypography.medium.sp16,
        color = WalletColors.styleguide_light_grey,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.93.dp)
      )
    }
    Row(
      Modifier
        .padding(bottom = 24.dp, end = 24.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {

      ButtonWithText(
        label = stringResource(id = R.string.cancel_button),
        onClick = {
                  onExitClick()
        },
        backgroundColor = Color.Transparent,
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.DEFAULT
      )

      ButtonWithText(
        label = stringResource(id = R.string.confirm_button),
        onClick = {
          onExitClick()
        },
        backgroundColor = WalletColors.styleguide_pink,
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.DEFAULT
      )
    }
  }
}