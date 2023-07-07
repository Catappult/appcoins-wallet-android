package com.appcoins.wallet.feature.backup.ui.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsState
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsViewModel
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupSuccessRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  onGotItClick: () -> Unit
) {
  Scaffold(
    topBar = {
      Surface {
        TopBar(isMainBar = false, onClickSupport = { onChatClick() })
      }
    },
    modifier = Modifier
  ) { padding ->
    BackupSaveOptionsScreen(
      scaffoldPadding = padding,
      onExitClick = onExitClick,
      onGotItClick = onGotItClick
    )
  }
}
@Composable
fun BackupSaveOptionsScreen(
  scaffoldPadding: PaddingValues,
  onExitClick: () -> Unit,
  onGotItClick: () -> Unit
) {
  Column(
    modifier = Modifier
    .fillMaxSize(1f)
    .padding(scaffoldPadding)
    .verticalScroll(rememberScrollState()),
  ) {
    Column(
      modifier = Modifier.padding(start = 27.dp, bottom = 45.dp),
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
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,

        )
    }
    BackupSuccessScreenCard()
    Spacer(modifier = Modifier.weight(10f))
    BackupSuccessButton(onGotItClick)
    }

  }


@Preview
@Composable
fun BackupSuccessScreenCard() {
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.padding(bottom = 112.dp, start= 16.dp, end = 16.dp, top = 45.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF242333))
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 36.6.dp, bottom = 57.dp)
    ) {
      WalletImage(
        Modifier
          .size(height = 62.4.dp, width = 46.32.dp),
        data = R.drawable.ic_lock_check
      )
      Text(
        text = "Backup done!", // trocar problema com string
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.bold.sp22,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp, bottom = 11.dp)
      )
      Text(
        text = "Your backup is on your device", // trocar problema com string
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.medium.sp14,
        textAlign = TextAlign.Center,
      )
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 19.dp, bottom = 49.dp),
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
          modifier = Modifier.size(5.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_1),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14,
          modifier = Modifier.padding(start = 7.dp)
        )

      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
      ) {
        WalletImage(
          data = R.drawable.ic_circle,
          modifier = Modifier.size(5.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_2),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14,
          modifier = Modifier.padding(start = 7.dp)
        )

      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        WalletImage(
          data = R.drawable.ic_circle,
          modifier = Modifier.size(5.dp)

        )
        Text(
          stringResource(id = R.string.backup_confirmation_tips_3),
          color = WalletColors.styleguide_light_grey,
          style = WalletTypography.regular.sp14,
          modifier = Modifier.padding(start = 7.dp)
        )

      }
    }
  }
}


@Composable
fun BackupSuccessButton(
  onGotItClick: () -> Unit
) {
  Column(
    Modifier.padding(
      start = 24.dp, end = 24.dp, bottom = 28.dp
    )
  ) {
    ButtonWithText(
      label = stringResource(id = R.string.got_it_button),
      onClick = { onGotItClick() },
      backgroundColor = WalletColors.styleguide_pink,
      labelColor = WalletColors.styleguide_light_grey,
      buttonType = ButtonType.LARGE,
    )
  }
}