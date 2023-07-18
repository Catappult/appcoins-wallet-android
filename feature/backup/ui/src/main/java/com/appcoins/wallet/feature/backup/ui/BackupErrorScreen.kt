package com.appcoins.wallet.feature.backup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.feature.backup.ui.success.BackupSaveOptionsScreen
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
fun BackupErrorRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
) {
  Scaffold(
    topBar = {
      Surface {
        TopBar(isMainBar = false, onClickSupport = { onChatClick() })
      }
    },
    modifier = Modifier
  ) { padding ->
    BackupErrorScreen(
      scaffoldPadding = padding,
      onExitClick = onExitClick,
    )
  }
}

@Composable
fun BackupErrorScreen(
  scaffoldPadding: PaddingValues,
  onExitClick: () -> Unit,
){
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
          end = 27.dp
        )
      )
      Text(
        text = stringResource(id = R.string.backup_body),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,

        )
    }
    BackupDialogCard(onExitClick = onExitClick)
  }
}


@Composable
fun BackupDialogCard(
  onExitClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary)
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
        data = R.drawable.ic_alert_circle_red
      )
      Text(
        text = stringResource(id = R.string.error_general),
        style = WalletTypography.bold.sp22,
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier.padding(top = 28.93.dp)
      )
      Text(
        text = "We are unable to create the backup file. Try again later", //criar string
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,
        textAlign = TextAlign.Center
      )
    }
    Row(
      Modifier.padding(top = 11.dp, bottom = 24.dp, end = 24.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {

      ButtonWithText(
        label = stringResource(id = R.string.cancel_button),
        onClick = {
          onExitClick
        },
        backgroundColor = Color.Transparent,
        labelColor = MaterialTheme.colorScheme.primaryContainer,
        buttonType = ButtonType.DEFAULT
      )

      ButtonWithText(
        label = stringResource(id = R.string.try_again),
        onClick = {
        },
        backgroundColor = WalletColors.styleguide_pink,
        labelColor = MaterialTheme.colorScheme.primaryContainer,
        buttonType = ButtonType.DEFAULT
      )
    }
  }
}
