package com.appcoins.wallet.feature.backup.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.backup.ui.BackupDialogCardAlertBottomSheet

import com.appcoins.wallet.feature.backup.ui.BackupErrorScreen
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import com.appcoins.wallet.ui.widgets.component.WalletTextFieldPassword
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupEntryRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  onNextClick: () -> Unit,
  viewModel: BackupEntryViewModel = hiltViewModel(),

) {
  val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val backupEntryState by viewModel.stateFlow.collectAsState()
  Scaffold(
    topBar = {
      Surface {
        TopBar(isMainBar = false, onClickSupport = { onChatClick() })
      }
    },
    modifier = Modifier
  ) { padding ->
    BackupEntryScreen(
      backupEntryState = backupEntryState,
      scaffoldPadding = padding,
      onExitClick = onExitClick,
      onNextClick = onNextClick,
      walletAddress = viewModel.walletAddress,
      onPasswordChange = {password ->
        viewModel.password = password
      },
      viewModel = viewModel,
      bottomSheetState = bottomSheetState
    )
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupEntryScreen(
  scaffoldPadding: PaddingValues,
  backupEntryState: BackupEntryState,
  onExitClick: () -> Unit,
  onNextClick: () -> Unit,
  onPasswordChange : (password : String) -> Unit,
  walletAddress: String,
  viewModel: BackupEntryViewModel,
  bottomSheetState: SheetState
) {
  var openBottomSheet by rememberSaveable{ mutableStateOf(true) }
  if(openBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = {
        openBottomSheet = false
      },
      sheetState = bottomSheetState,
      containerColor = WalletColors.styleguide_blue
    ) {
      BackupDialogCardAlertBottomSheet(
        onExitClick = {openBottomSheet = false}
      )
    }
  }
   when (val balanceInfo = backupEntryState.balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO add wallet animation loading and change it to png or xml
        //WalletImage(data = R.drawable.ic_loadingWalletInside)
      }
      is Async.Success -> {
        Column (
          modifier = Modifier
            .fillMaxSize(1f)
            .padding(scaffoldPadding)
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.Start,

            ){
          Text(
            style = WalletTypography.bold.sp22,
            color = WalletColors.styleguide_light_grey,
            text = stringResource(id = R.string.backup_title),
            modifier = Modifier
              .padding(
              top = 10.dp,
              bottom = 20.dp,
              start = 27.dp
            )
          )
          Text(
            text = stringResource(id = R.string.backup_body),
            modifier = Modifier.padding(
              bottom = 30.dp,
              start = 27.dp
            ),
            style = WalletTypography.medium.sp14,
            color = WalletColors.styleguide_light_grey,

          )
          BalanceCard(
            "${balanceInfo.value?.amount}${balanceInfo.value?.symbol}",
            walletAddress,
            onPasswordChange = onPasswordChange,
            viewModel = viewModel
          )
          Spacer(modifier = Modifier.weight(10f))
          BackupEntryButtonPasswordsCorrect(onNextClick, viewModel = viewModel)
          }


      }
      is Async.Fail -> Unit
    }
  }

@Composable
fun BalanceCard(balance : String, walletAddress: String, onPasswordChange: (password : String) -> Unit, viewModel: BackupEntryViewModel) {
  Card(
    elevation = CardDefaults.cardElevation(8.dp),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .padding(16.dp)
      .fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF242333))
  ) {
    Card(
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24))

    ) {
      Text(
        text = balance,
        style = WalletTypography.bold.XXS,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .padding(bottom = 4.dp)
      )
      Text(
        text = walletAddress,

      )
    }
    BackupEntryPassword(onPasswordChange = onPasswordChange, viewModel = viewModel)
      }
  }




@Composable
fun BackupEntryButtonPasswordsCorrect( // why it do not refresh?
  onNextClick: () -> Unit,
  viewModel: BackupEntryViewModel
) {
Column(
  Modifier.padding(
    start = 24.dp, end = 24.dp, bottom = 28.dp
  )
) {
  ButtonWithText(
    label = stringResource(id = R.string.backup_wallet_button),
    onClick = {
      if (viewModel.correctInputPassword.value) onNextClick() else {
      }
    },
    backgroundColor = if (viewModel.correctInputPassword.value) WalletColors.styleguide_pink else WalletColors.styleguide_dark_grey,
    labelColor = WalletColors.styleguide_light_grey,
    buttonType = ButtonType.LARGE,
  )
}
}

@Composable
fun BackupEntryPassword(onPasswordChange: (password : String) -> Unit, viewModel: BackupEntryViewModel) {
  var switchON by rememberSaveable { mutableStateOf(false) }
    Column(
      modifier = Modifier
        .padding(18.dp)
        .fillMaxWidth(),
    ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            horizontalAlignment = Alignment.Start
          ) {
            Text(
              text = stringResource(R.string.backup_additional_security_title),
              style = WalletTypography.bold.sp14,
              color = WalletColors.styleguide_light_grey,
              modifier = Modifier.padding(bottom = 8.21.dp, end = 68.dp)
            )
            Text(
              text = stringResource(R.string.backup_additional_security_body),
              style = WalletTypography.regular.sp14,
              modifier = Modifier.padding(bottom = 21.dp),
              color = WalletColors.styleguide_dark_grey,
            )
          }

          Switch(
            modifier = Modifier.padding(end = 8.dp),
            checked = switchON,
            onCheckedChange = {
                changedSwitch -> switchON = changedSwitch
                viewModel.correctInputPassword.value = true
                              },
            colors = SwitchDefaults.colors(
              checkedThumbColor = WalletColors.styleguide_pink,
              checkedTrackColor = WalletColors.styleguide_medium_grey,
              uncheckedThumbColor = WalletColors.styleguide_light_grey,
              uncheckedIconColor = Color.Transparent ,
              uncheckedTrackColor= WalletColors.styleguide_medium_grey,
              checkedBorderColor = Color.Transparent,
              uncheckedBorderColor = Color.Transparent

            )
          )
        }
      if(switchON){
        switchModeTrue(onPasswordChange = onPasswordChange, viewModel = viewModel)
      }
    }
  }


@Composable
private fun switchModeTrue(onPasswordChange: (password : String) -> Unit, viewModel: BackupEntryViewModel) {
  var defaultPassword by rememberSaveable { mutableStateOf("") }
  var defaultPassword2 by rememberSaveable { mutableStateOf("") }

  AnimatedVisibility(
    visible = true,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      WalletTextFieldPassword(value = defaultPassword,
        onValueChange = {  defaultPassword = it
        onPasswordChange(it)
        },
        hintText = R.string.password
      )
      WalletTextFieldPassword(value = defaultPassword2,
        onValueChange = { defaultPassword2 = it },
        hintText = R.string.repeat_password
      )
      if (defaultPassword.isEmpty() || defaultPassword2.isEmpty()){
        showPasswordError(false)
        viewModel.correctInputPassword.value = false
      }else if (defaultPassword.isNotEmpty() && defaultPassword != defaultPassword2){
        showPasswordError(true)
        viewModel.correctInputPassword.value = false
      } else{
        showPasswordError(false)
        viewModel.correctInputPassword.value = true
      }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        WalletImage(
          data = R.drawable.ic_alert_circle_red,
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
        )
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
        ){
        Text(
          text = stringResource(R.string.backup_additional_security_disclaimer_body),
          style = WalletTypography.bold.sp12,
          color = WalletColors.styleguide_pink,
        )
        Text(
          text = stringResource(R.string.backup_additional_security_disclaimer_title),
          style = WalletTypography.regular.sp12,
          color = WalletColors.styleguide_light_grey
        )
        }
      }
    }
  }
}

@Composable
private fun showPasswordError(shouldShow: Boolean) {

  if (shouldShow) {
    Text(text = stringResource(id = R.string.backup_additional_security_password_not_march),
    style = WalletTypography.regular.sp12,
    color = WalletColors.styleguide_red)
  }
}

@Preview("screen")
@Composable
private fun BackupEntryScreenPreview() {
  //BalanceCard(balance = "sdf", walletAddress = "asd", onPasswordChange = {"password"}, viewModel = viewModel
  //))
}






