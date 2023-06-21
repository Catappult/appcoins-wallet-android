package com.appcoins.wallet.feature.backup.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText


@Composable
fun BackupEntryRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  viewModel: BackupEntryViewModel = hiltViewModel(),
) {
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
      walletAddress = viewModel.walletAddress
    )
  }
}

@Composable
fun BackupEntryScreen(
  scaffoldPadding: PaddingValues,
  backupEntryState: BackupEntryState,
  onExitClick: () -> Unit,
  walletAddress: String
) {

    when (val balanceInfo = backupEntryState.balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO add wallet animation loading and change it to png or xml
        //WalletImage(data = R.drawable.ic_loadingWalletInside)
      }
      is Async.Success -> {
        Column (
          Modifier.padding(scaffoldPadding),
          horizontalAlignment = Alignment.Start
            ){
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
          BalanceCard("${balanceInfo.value?.amount} ${balanceInfo.value?.symbol}", walletAddress) //melhorar
          BackupEntryButton()
        }


      }
      is Async.Fail -> Unit
    }
  }

@Composable
fun BalanceCard(balance : String, walletAddress: String) {
  Card(
    elevation = CardDefaults.cardElevation(8.dp),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .padding(8.dp)
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
    BackupEntryPassword()
      }
  }




@Preview
@Composable
fun BackupEntryButton() {
  ButtonWithText(
    label = stringResource(id = R.string.backup_wallet_button),
    onClick = {},
    backgroundColor = WalletColors.styleguide_pink,
    labelColor = WalletColors.styleguide_light_grey,
    buttonType = ButtonType.LARGE,
  )
}

@Preview
@Composable
fun BackupEntryPassword() {
  var switchON by rememberSaveable { mutableStateOf(true) }
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
              modifier = Modifier.padding(bottom = 21.dp)
            )
          }
          Switch(
            modifier = Modifier.padding(end = 3.dp),
            checked = switchON,
            onCheckedChange = { changedSwitch -> switchON = changedSwitch },
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
        switchModeTrue()
      }
    }
  }


@Composable
private fun switchModeTrue() {
  var defaultPassword by rememberSaveable { mutableStateOf("Password") }
  var defaultPassword2 by rememberSaveable { mutableStateOf("Repeat Password") }
  AnimatedVisibility(
    visible = true,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      TextField( //WalletTextfield esperar
        value = defaultPassword,
        modifier = Modifier.padding(bottom = 12.dp),
        onValueChange = {
          defaultPassword = it
        }

      )
      TextField(
        value = defaultPassword2,
        onValueChange = {
          defaultPassword2 = it
        }
      )
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

@Preview("screen")
@Composable
private fun BackupEntryScreenPreview() {
  BalanceCard(balance = "sdf", walletAddress = "asd")
}






