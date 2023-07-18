package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextFieldCustom

lateinit var passwordInput: String

@Composable
fun BackupSaveOptionsRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  onSendEmailClick: () ->Unit,
  onSaveOnDevice: () -> Unit,
  viewModel: BackupSaveOptionsViewModel = hiltViewModel(),
) {
  val backupSaveOptionsState by viewModel.stateFlow.collectAsState()
  Scaffold(
    topBar = {
      Surface {
        TopBar(isMainBar = false, onClickSupport = { onChatClick() })
      }
    },
    modifier = Modifier
  ) { padding ->
    BackupSaveOptionsScreen(
      backupSaveOptionsState = backupSaveOptionsState,
      scaffoldPadding = padding,
      onExitClick = onExitClick,
      onSendEmailClick = { viewModel.sendBackupToEmail(passwordInput) },
      onSaveOnDevice = onSaveOnDevice
    )
  }
}
@Composable
fun BackupSaveOptionsScreen(
  scaffoldPadding: PaddingValues,
  backupSaveOptionsState: BackupSaveOptionsState,
  onSaveOnDevice: () -> Unit,
  onExitClick: () -> Unit,
  onSendEmailClick: () ->Unit,

) {
  Column(
    modifier = Modifier
      .fillMaxSize(1f)
      .padding(scaffoldPadding)
      .verticalScroll(
        rememberScrollState(),
      )) {
    Column(
      modifier = Modifier.padding(start = 27.dp,top = 10.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        style = WalletTypography.bold.sp22,
        color = WalletColors.styleguide_light_grey,
        text = stringResource(id = R.string.backup_title),
        modifier = Modifier.padding(
          bottom = 20.dp,
        )
      )
      Text(
        text = stringResource(id = R.string.backup_body),
        modifier = Modifier.padding(
          bottom = 4.dp, end = 27.dp
        ),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,

        )
    }
    SaveOnDeviceCardDefault()
    SaveOnDeviceOptions(
      onSendEmailClick,
      onExitClick = onExitClick,
      onSaveOnDevice = onSaveOnDevice,
    )


  }
}


@Preview
@Composable
fun SaveOnDeviceCardDefault(){
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 40.dp, top = 40.dp, start = 16.dp, end = 16.dp),
    colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(33.6.dp)
    ) {
      WalletImage(
        Modifier.size(46.32.dp, 62.4.dp),
        data = R.drawable.ic_lock_appc
      )
      Text(
        text = stringResource(id = R.string.backup_ready_title),
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.bold.sp22,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
      )
    }
  }
}
@Composable
fun SaveOnDeviceOptions(
  onSendEmailClick: () -> Unit,
  onSaveOnDevice: () -> Unit,
  onExitClick: () -> Unit,
) {
  var defaultEmail by rememberSaveable { mutableStateOf("") }
  var validEmail by rememberSaveable { mutableStateOf(false) }

  Column(
    Modifier.fillMaxWidth()
  ) {
    Column(
      Modifier.padding(start = 24.dp, bottom = 12.dp, end = 24.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = stringResource(id = R.string.backup_ready_save_on_email),
        Modifier.padding(bottom = 8.dp),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey
      )
      WalletTextFieldCustom(
        value = defaultEmail,
        onValueChange = {
          defaultEmail = it
          passwordInput = defaultEmail
        },
        hintText = R.string.email_here_field
      )
      if(defaultEmail.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS
          .matcher(defaultEmail).matches()){
        validEmail = true
      }
      ButtonWithText(
        label = stringResource(id = R.string.backup_ready_email_button),
        onClick = {
          if(validEmail) onSendEmailClick() else {}
        },
        backgroundColor = if (validEmail) WalletColors.styleguide_pink else styleguide_dark_grey,
        labelColor = WalletColors.styleguide_light_grey,
        buttonType = ButtonType.LARGE,
      )
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(top = 1.25.dp, bottom = 8.75.dp)
    ) {
      Divider(
        modifier = Modifier
          .weight(1f)
          .height(1.dp),
        color = styleguide_dark_grey
      )
      Text(
        text = stringResource(R.string.common_or),
        color = styleguide_dark_grey, style = WalletTypography.regular.sp12,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
      Divider(
        modifier = Modifier
          .weight(1f)
          .height(1.dp),
        color = styleguide_dark_grey
      )
    }
    Column(
      modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 27.dp)
    ) {
      ButtonWithText(
        label = "SEND BACKUP", // String ??
        onClick = {
          onSaveOnDevice()
        },
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.LARGE,
        outlineColor = WalletColors.styleguide_light_grey
      )
    }
  }
}
@Preview
@Composable
fun BackupSaveOptionsScreenPreview(

  ) {

  Column(
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
    SaveOnDeviceCardDefault()
    SaveOnDeviceOptionsPreview()


  }
}

  @Composable
  fun SaveOnDeviceOptionsPreview(

  ) {
    var defaultEmail by rememberSaveable { mutableStateOf("Your email here...") }

    Column(
      Modifier.fillMaxWidth()
    ) {
      Column(
        Modifier.padding(8.dp),
        horizontalAlignment = Alignment.Start
      ) {
        Text(
          text = stringResource(id = R.string.backup_ready_save_on_email),
          Modifier.padding(bottom = 17.75.dp),
          style = WalletTypography.medium.sp14,
          color = WalletColors.styleguide_light_grey
        )
        WalletTextFieldCustom(value = defaultEmail,
          onValueChange = {
            defaultEmail = it
        },
          hintText = R.string.email_here_field
          )
        ButtonWithText(
          label = stringResource(id = R.string.backup_ready_email_button),
          onClick = {

          },
          backgroundColor = WalletColors.styleguide_pink,
          labelColor = WalletColors.styleguide_light_grey,
          buttonType = ButtonType.LARGE,
        )
      }
      Row( //why not full screen?
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .padding(top = 14.25.dp, bottom = 17.75.dp)
      ) {
        Divider(
          modifier = Modifier
            .weight(1f)
            .height(1.dp),
          color = styleguide_dark_grey
        )
        Text(
          text = stringResource(R.string.common_or),
          color = styleguide_dark_grey, style = WalletTypography.regular.sp12,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
          modifier = Modifier
            .weight(1f)
            .height(1.dp),
          color = styleguide_dark_grey
        )
      }
      ButtonWithText(
        label = "SEND BACKUP", // String ??
        onClick = {
        },
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.LARGE,
        outlineColor = WalletColors.styleguide_light_grey
      )
    }

  }


