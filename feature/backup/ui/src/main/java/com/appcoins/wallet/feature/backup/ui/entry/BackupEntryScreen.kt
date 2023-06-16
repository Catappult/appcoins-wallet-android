package com.appcoins.wallet.feature.backup.ui.entry

import android.service.quickaccesswallet.WalletCard
import android.widget.Space
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.feature.backup.data.Balance
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
      is Async.Loading -> Unit
      is Async.Success -> {
        Column (
          Modifier.padding(scaffoldPadding)
            ){
          Text(
            style = WalletTypography.bold.sp22,
            color = WalletColors.styleguide_light_grey,
            text = stringResource(id = R.string.backup_title),
            textAlign = TextAlign.Left,
            modifier = Modifier.padding(
              start = 27.dp,
              top = 10.dp,
              bottom = 20.dp,
            )
          )
          Text(
            text = stringResource(id = R.string.backup_body),
            modifier = Modifier.padding(
              start = 27.dp,
              top = 20.dp,
              bottom = 45.dp,
            ),
            textAlign = TextAlign.Left,
            style = WalletTypography.medium.sp14,
            color = WalletColors.styleguide_light_grey,

          )
          BalanceCard("${balanceInfo.value?.amount} ${balanceInfo.value?.symbol}", walletAddress)
          BackupEntryButton()
        }


      } //-> nao fazer uma lista as montar o screen com o card a parte e ir buscar informaçoes da wallet address e balance
      is Async.Fail -> Unit
    }
  }

@Composable
fun BalanceCard(balance : String, walletAddress: String) {
  Card(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.padding(14.dp),
  ) {
    Card(
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.padding(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0x1A1A24))

    ) {
      Text(
        text = balance,
        //style = WalletBackupTextStyle.BoldWhiteBig,
        style = WalletTypography.bold.XXS,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
      )
      Text(
        text = walletAddress,
        //style = WalletBackupTextStyle.Address,
        modifier = Modifier.fillMaxWidth().padding(
          start = 16.dp
        )
      )
    }
        PasswordToggle()
      }
  }





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


@Composable
fun BackupEntryPassword() {
  var switchON by rememberSaveable { mutableStateOf(true) }
    Column {
        Row(
        ) {
          Column() {
            Text(
              text = stringResource(R.string.backup_additional_security_title),
              style = WalletTypography.bold.sp14,
              color = WalletColors.styleguide_light_grey,
              textAlign = TextAlign.Left,
              modifier = Modifier.padding(start = 18.dp, top = 34.79.dp, bottom = 8.21.dp, end = 70.dp)
            )
            Text(
              text = stringResource(R.string.backup_additional_security_body),
              modifier = Modifier.padding(start = 17.dp, top = 8.21.dp, bottom = 24.dp, end = 130.dp),
              style = WalletTypography.regular.sp14,
              textAlign = TextAlign.Left
            )
          }
          Switch(
            modifier = Modifier.padding(start = 71.dp, top = 47.79.dp, bottom = 34.21.dp, end = 21.dp),
            checked = switchON,
            onCheckedChange = { changedSwitch -> switchON = changedSwitch }
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
        .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
      TextField(
        value = defaultPassword,
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp)
      ) {
        WalletImage(
          data = R.drawable.ic_alert_circle_red,
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
        )
        Column(){
        Text(
          text = stringResource(R.string.backup_additional_security_disclaimer_body),
          //style = WalletBackupWarningStyle.Bold,
          modifier = Modifier
            .padding(horizontal = 16.dp)
        )
        Text(
          text = stringResource(R.string.backup_additional_security_disclaimer_title),
          //style = WalletBackupTextStyle.Normal.White.S,
          modifier = Modifier
            .padding(horizontal = 16.dp)
        )
        }
      }
    }
  }
}


@Preview("password")
@Composable
private fun PasswordToggle() {
  BackupEntryPassword()
}

@Preview("button")
@Composable
private fun BackupEntryButtonPreview() {
  BackupEntryButton()
}

@Preview("screen")
@Composable
private fun BackupEntryScreenPreview() {
  BalanceCard(balance = "sdf", walletAddress = "asd")
}






