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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryScreen
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryState
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryViewModel
import com.appcoins.wallet.feature.backup.ui.save_on_device.BackupSaveOnDeviceDialogRoute
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import kotlinx.coroutines.launch
import kotlin.math.round

lateinit var passwordInput: String
@Composable
fun BackupSaveOptionsRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  onSendEmailClick: () ->Unit,
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
    )
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSaveOptionsScreen(
  scaffoldPadding: PaddingValues,
  backupSaveOptionsState: BackupSaveOptionsState,
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
          bottom = 45.dp,
        ),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,

        )
    }
    SaveOnDeviceCardDefault()
    SaveOnDeviceOptions(
      onSendEmailClick,
      onExitClick = onExitClick,
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
      .padding(bottom = 62.62.dp, top = 45.dp, start = 16.dp, end = 16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF242333))
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
        text = stringResource(id = R.string.backup_ready_title), // trocar problema com string
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.bold.sp22,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp, bottom = 51.dp)
      )
    }
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveOnDeviceOptions(
  onSendEmailClick: () -> Unit,
  onExitClick: () -> Unit,
) {
  var defaultEmail by rememberSaveable { mutableStateOf("") }
  var validEmail by rememberSaveable { mutableStateOf(false) }

  Column(
    Modifier.fillMaxWidth()
  ) {
    Column(
      Modifier.padding(24.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = stringResource(id = R.string.backup_ready_save_on_email),
        Modifier.padding(bottom = 17.75.dp),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey
      )
      WalletTextField(
        value = defaultEmail, //mudar cor de background
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
        backgroundColor = if (validEmail) WalletColors.styleguide_pink else WalletColors.styleguide_dark_grey,
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
        color = Color(0xFF707070)
      )
      Text(
        text = stringResource(R.string.common_or),
        color = Color(0xFF707070), style = WalletTypography.regular.sp12,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
      Divider(
        modifier = Modifier
          .weight(1f)
          .height(1.dp),
        color = Color(0xFF707070)
      )
    }
    Column(
      modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 27.dp)
    ) {
      ButtonWithText(
        label = "SEND BACKUP", // -> problema com as strings trocar padding???
        onClick = {
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
  //val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        WalletTextField(value = defaultEmail,
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
          color = Color(0xFF707070)
        )
        Text(
          text = stringResource(R.string.common_or),
          color = Color(0xFF707070), style = WalletTypography.regular.sp12,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
          modifier = Modifier
            .weight(1f)
            .height(1.dp),
          color = Color(0xFF707070)
        )
      }
      ButtonWithText(
        label = "SEND BACKUP", // -> problema com as strings trocar padding???
        onClick = {
        },
        labelColor = WalletColors.styleguide_white,
        buttonType = ButtonType.LARGE,
        outlineColor = WalletColors.styleguide_light_grey
      )
    }

  }


/*
@Preview
@Composable
fun BackupSaveOptionsScreen(){

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
    SaveOnDeviceCardDefault()
    SaveOnDeviceOptions()

  }
  }
 */



