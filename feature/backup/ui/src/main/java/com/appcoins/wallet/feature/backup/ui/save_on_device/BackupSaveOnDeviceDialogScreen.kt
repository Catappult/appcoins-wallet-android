package com.appcoins.wallet.feature.backup.ui.save_on_device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryScreen
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryState
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryViewModel
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

// a trocar
@Composable
fun BackupSaveOnDeviceDialogRoute(

  viewModel: BackupSaveOnDeviceDialogViewModel = hiltViewModel(),
) {
    BackupSaveOnDeviceScreen(
    )

  val backupSaveOnDeviceDialogState by viewModel.stateFlow.collectAsState()
  when (backupSaveOnDeviceDialogState.fileName) {
    Async.Uninitialized,
    is Async.Loading -> {
      //TODO add wallet animation loading and change it to png or xml
      //WalletImage(data = R.drawable.ic_loadingWalletInside)
    }
    is Async.Success -> {
    }

    is Async.Fail -> Unit
  }


  }



@Composable
fun BackupSaveOnDeviceScreen(
) {
  var defaultBackup by rememberSaveable { mutableStateOf("BackupFile") }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(25.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = stringResource(R.string.backup_save_dialogue_title),
      style = WalletTypography.bold.sp12,
      color = WalletColors.styleguide_light_grey,
      textAlign = TextAlign.Left,
      modifier = Modifier.padding(top = 34.dp)
    )
    Text(
      text = stringResource(R.string.backup_save_dialogue_body),
      style = WalletTypography.medium.sp12,
      color = WalletColors.styleguide_pink,
      modifier = Modifier.padding(top = 3.dp, bottom = 6.dp)
    )

    TextField( //WalletTextfield esperar
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 32.dp),
      value = defaultBackup,
      onValueChange = {
        defaultBackup = it
      },
      colors = TextFieldDefaults.colors(
        unfocusedContainerColor = WalletColors.styleguide_blue_secondary,
        focusedContainerColor = WalletColors.styleguide_blue_secondary,
        focusedLabelColor = Color.Red //-> ? cor?
      )
    )
    BackupSaveOnDeviceButton()
  }
}

@Preview
@Composable
fun BackupSaveOnDeviceDialogView() {
  var defaultBackup by rememberSaveable { mutableStateOf("BackupFile") }
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(25.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = stringResource(R.string.backup_save_dialogue_title),
      style = WalletTypography.bold.sp12,
      color = WalletColors.styleguide_light_grey,
      textAlign = TextAlign.Left,
      modifier = Modifier.padding(top = 34.dp)
    )
    Text(
      text = stringResource(R.string.backup_save_dialogue_body),
      style = WalletTypography.medium.sp12,
      color = WalletColors.styleguide_pink,
      modifier = Modifier.padding(top = 3.dp, bottom = 6.dp)
    )
    TextField( //WalletTextfield esperar
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 32.dp),
      value = defaultBackup,
      onValueChange = {
        defaultBackup = it
      },
      colors = TextFieldDefaults.colors(
        unfocusedContainerColor = WalletColors.styleguide_blue_secondary,
        focusedContainerColor = WalletColors.styleguide_blue_secondary,
        focusedLabelColor = Color.Red //-> ? cor?
      )
    )
    BackupSaveOnDeviceButton()
  }
}



@Composable
fun BackupSaveOnDeviceButton(){
  ButtonWithText(
    label = stringResource(id = R.string.action_save),
    onClick = {

    },
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = MaterialTheme.colorScheme.primaryContainer,
    buttonType = ButtonType.LARGE
  )
}