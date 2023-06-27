package com.appcoins.wallet.feature.backup.ui.skip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryScreen
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryViewModel
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

/*
@Composable
fun BackupSkipDialogRoute(
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
    BackupSkipDialogScreen(
     // scaffoldPadding = padding,
     // onExitClick = onExitClick,
    )
  }
}

 */
@Preview
@Composable
fun BackupSkipDialogScreen(
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(37.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    WalletImage(
      Modifier
        .size(68.57.dp),
      data = R.drawable.ic_alert_circle_red
    )
    Text(
      text = stringResource(id = R.string.backup_skip_title),
      style = WalletTypography.bold.sp16,
      color = WalletColors.styleguide_light_grey,
      modifier = Modifier.padding(top = 24.93.dp)
    )
    Row(
      Modifier.padding(top = 27.dp, end = 24.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {

      ButtonWithText(
        label = stringResource(id = R.string.cancel_button),
        onClick = {
        },
        backgroundColor = Color.Transparent,
        labelColor = MaterialTheme.colorScheme.primaryContainer,
        buttonType = ButtonType.DEFAULT
      )

      ButtonWithText(
        label = stringResource(id = R.string.confirm_button),
        onClick = {
        },
        backgroundColor = WalletColors.styleguide_pink,
        labelColor = MaterialTheme.colorScheme.primaryContainer,
        buttonType = ButtonType.DEFAULT
      )

    }

  }
}