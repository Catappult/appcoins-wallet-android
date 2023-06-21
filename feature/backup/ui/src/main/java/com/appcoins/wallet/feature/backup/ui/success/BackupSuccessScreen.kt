package com.appcoins.wallet.feature.backup.ui.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Preview
@Composable
fun BackupSaveOptionsScreen() {
  Column(
    modifier = Modifier.padding(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(11.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        style = WalletTypography.bold.sp22,
        color = WalletColors.styleguide_light_grey,
        text = stringResource(id = R.string.backup_title),
        modifier = Modifier.padding(
          top = 10.dp,
          bottom = 20.dp,
        )
      )
      Text(
        text = stringResource(id = R.string.backup_body),
        modifier = Modifier.padding(
          bottom = 45.dp,
        ),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,

        )
    }
    BackupSuccessScreenCard()
    BackupSuccessButton()
    }

  }


@Preview
@Composable
fun BackupSuccessScreenCard() {
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth().padding(bottom = 110.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF242333))
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(40.dp)
    ) {
      WalletImage(
        Modifier
          .size(40.dp),
        data = R.drawable.ic_lock_check
      )
      Text(
        text = "Backup done!", // trocar problema com string
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.bold.sp22,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp)
      )
      Text(
        text = "Your backup is on your device", // trocar problema com string
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.medium.sp14,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 10.dp)
      )
    }
    Column(
      modifier = Modifier // -> qual padding entre rows?
        .fillMaxWidth()
        .padding(19.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = stringResource(id = R.string.backup_confirmation_tips_title),
        style = WalletTypography.bold.sp16,
        color = WalletColors.styleguide_light_grey
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp)
      ) {
        WalletImage(
          data = R.drawable.ic_circle,
          modifier = Modifier.size(16.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_1),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14
        )

      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
      ) {
        WalletImage(
          data = R.drawable.ic_circle,
          modifier = Modifier.size(16.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_2),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14
        )

      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)
      ) {
        WalletImage(
          data = R.drawable.ic_circle,
          modifier = Modifier.size(16.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_3),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14
        )

      }
    }
  }
}

@Preview
@Composable
fun BackupSuccessButton(){
  ButtonWithText(
    label = stringResource(id = R.string.got_it_button),
    onClick = {},
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_light_grey,
    buttonType = ButtonType.LARGE,
  )
}