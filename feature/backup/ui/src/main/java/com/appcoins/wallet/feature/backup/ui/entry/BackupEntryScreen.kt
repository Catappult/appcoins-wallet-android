package com.appcoins.wallet.feature.backup.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.maskedEnd
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.simpleFormat
import com.appcoins.wallet.feature.backup.ui.BackupDialogCardAlertBottomSheet
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextFieldPassword
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupEntryRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  onNextClick: () -> Unit,
  onChooseWallet: () -> Unit,
  viewModel: BackupEntryViewModel = hiltViewModel()
) {
  val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val backupEntryState by viewModel.stateFlow.collectAsState()
  Scaffold(
    topBar = {
      Surface {
        TopBar(onClickSupport = { onChatClick() }, onClickBack = { viewModel.showBottomSheet() })
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
      walletName = viewModel.walletName,
      onPasswordChange = { password -> viewModel.password = password },
      isInputPasswordCorrect = viewModel.correctInputPassword.value,
      onChooseWallet = onChooseWallet,
      bottomSheetState = bottomSheetState,
      onInputPasswordIsCorrect = { viewModel.correctInputPassword.value = true },
      onInputPasswordIsIncorrect = { viewModel.correctInputPassword.value = false },
      showBottomSheet = viewModel.showBottomSheet.value,
      dismissBottomSheet = { viewModel.showBottomSheet(false) })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupEntryScreen(
  scaffoldPadding: PaddingValues,
  backupEntryState: BackupEntryState,
  onExitClick: () -> Unit,
  onNextClick: () -> Unit,
  onChooseWallet: () -> Unit,
  onPasswordChange: (password: String) -> Unit,
  onInputPasswordIsCorrect: () -> Unit,
  onInputPasswordIsIncorrect: () -> Unit,
  walletAddress: String,
  walletName: String,
  isInputPasswordCorrect: Boolean,
  bottomSheetState: SheetState,
  showBottomSheet: Boolean,
  dismissBottomSheet: () -> Unit
) {
  val scope = rememberCoroutineScope()

  if (showBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = dismissBottomSheet,
      sheetState = bottomSheetState,
      containerColor = styleguide_blue_secondary
    ) {
      BackupDialogCardAlertBottomSheet(
        onCancelClick = {
          scope.launch { bottomSheetState.hide() }.invokeOnCompletion { dismissBottomSheet() }
        },
        onConfirmClick = {
          dismissBottomSheet()
          onExitClick()
        })
    }
  }
  when (val balanceInfo = backupEntryState.balanceAsync) {
    Async.Uninitialized,
    is Async.Loading -> {
      // TODO add wallet animation loading and change it to png or xml
    }
    is Async.Success -> {
      Column(
        modifier =
        Modifier
          .fillMaxSize(1f)
          .padding(scaffoldPadding)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
      ) {
        Text(
          style = WalletTypography.bold.sp22,
          color = WalletColors.styleguide_light_grey,
          text = stringResource(id = R.string.backup_title),
          modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 24.dp)
        )
        Text(
          text = stringResource(id = R.string.backup_body),
          modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
          style = WalletTypography.medium.sp14,
          color = WalletColors.styleguide_light_grey,
        )
        BalanceCard(
          "${balanceInfo.value?.amount}${balanceInfo.value?.symbol}",
          walletAddress.maskedEnd(),
          "${walletName.simpleFormat()} - ",
          onPasswordChange = onPasswordChange,
          onChooseWallet = onChooseWallet,
          onInputPasswordIsCorrect = onInputPasswordIsCorrect,
          onInputPasswordIsIncorrect = onInputPasswordIsIncorrect
        )
        Spacer(modifier = Modifier.weight(10f))
        BackupEntryButtonPasswordsCorrect(onNextClick, isInputPasswordCorrect)
      }
    }
    is Async.Fail -> Unit
  }
}

@Composable
fun BalanceCard(
  balance: String,
  walletAddress: String,
  walletName: String,
  onPasswordChange: (password: String) -> Unit,
  onChooseWallet: () -> Unit,
  onInputPasswordIsCorrect: () -> Unit,
  onInputPasswordIsIncorrect: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }

  Card(
    elevation = CardDefaults.cardElevation(8.dp),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary)
  ) {
    Card(
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .padding(vertical = 16.dp, horizontal = 8.dp)
        .fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = styleguide_blue)
    ) {
      Row(
        modifier = Modifier
          .padding(start = 16.dp)
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.padding(bottom = 11.dp)) {
          Row {
            Text(
              text = walletName,
              style = WalletTypography.medium.sp14,
              maxLines = 1,
              color = WalletColors.styleguide_light_grey,
              modifier = Modifier.padding(bottom = 4.dp, top = 11.dp)
            )

            Text(
              text = balance,
              style = WalletTypography.medium.sp14,
              color = WalletColors.styleguide_light_grey,
              maxLines = 1,
              modifier = Modifier.padding(top = 11.dp)
            )
          }
          Text(
            text = walletAddress,
            style = WalletTypography.regular.sp12,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            color = WalletColors.styleguide_dark_grey
          )
        }

        Icon(
          painter = painterResource(id = R.drawable.ic_arrow_head_down),
          contentDescription = "show password",
          modifier =
          Modifier
            .height(40.dp)
            .width(48.dp)
            .padding(end = 16.dp)
            .clickable(
              interactionSource = interactionSource,
              indication = null,
              onClick = { onChooseWallet() }),
          tint = WalletColors.styleguide_pink
        )
      }
    }
    BackupEntryPassword(
      onPasswordChange = onPasswordChange,
      onInputPasswordIsCorrect,
      onInputPasswordIsIncorrect
    )
  }
}

@Composable
fun BackupEntryButtonPasswordsCorrect(onNextClick: () -> Unit, isInputPasswordCorrect: Boolean) {
  Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 28.dp)) {
    ButtonWithText(
      label = stringResource(id = R.string.backup_wallet_button),
      onClick = { if (isInputPasswordCorrect) onNextClick() },
      backgroundColor =
      if (isInputPasswordCorrect) WalletColors.styleguide_pink
      else WalletColors.styleguide_dark_grey,
      labelColor = WalletColors.styleguide_light_grey,
      buttonType = ButtonType.LARGE,
    )
  }
}

@Composable
fun BackupEntryPassword(
  onPasswordChange: (password: String) -> Unit,
  onInputPasswordIsCorrect: () -> Unit,
  onInputPasswordIsIncorrect: () -> Unit
) {
  var switchON by rememberSaveable { mutableStateOf(false) }
  Column(
    modifier = Modifier
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .padding(bottom = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        Text(
          text = stringResource(R.string.backup_additional_security_title),
          style = WalletTypography.bold.sp14,
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
          text = stringResource(R.string.backup_additional_security_body),
          style = WalletTypography.regular.sp14,
          color = WalletColors.styleguide_dark_grey,
        )
      }

      Switch(
        checked = switchON,
        onCheckedChange = { changedSwitch ->
          switchON = changedSwitch
          onInputPasswordIsCorrect()
        },
        colors =
        SwitchDefaults.colors(
          checkedThumbColor = WalletColors.styleguide_pink,
          uncheckedThumbColor = WalletColors.styleguide_light_grey,
          checkedTrackColor = WalletColors.styleguide_grey_blue,
          uncheckedTrackColor = WalletColors.styleguide_grey_blue,
          checkedBorderColor = Color.Transparent,
          uncheckedBorderColor = Color.Transparent
        )
      )
    }

    if (switchON) {
      SwitchModeTrue(
        onPasswordChange = onPasswordChange, onInputPasswordIsCorrect, onInputPasswordIsIncorrect
      )
    }
  }
}

@Composable
private fun SwitchModeTrue(
  onPasswordChange: (password: String) -> Unit,
  onInputPasswordIsCorrect: () -> Unit,
  onInputPasswordIsIncorrect: () -> Unit
) {
  var defaultPassword by rememberSaveable { mutableStateOf("") }
  var defaultPassword2 by rememberSaveable { mutableStateOf("") }

  AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
    Column(modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp)) {
      WalletTextFieldPassword(
        value = defaultPassword,
        onValueChange = {
          defaultPassword = it
          onPasswordChange(it)
        },
        hintText = R.string.password
      )
      WalletTextFieldPassword(
        value = defaultPassword2,
        onValueChange = { defaultPassword2 = it },
        hintText = R.string.repeat_password
      )
      if (defaultPassword.isEmpty() || defaultPassword2.isEmpty()) {
        ShowPasswordError(false)
        onInputPasswordIsIncorrect()
      } else if (defaultPassword.isNotEmpty() && defaultPassword != defaultPassword2) {
        ShowPasswordError(true)
        onInputPasswordIsIncorrect()
      } else {
        ShowPasswordError(false)
        onInputPasswordIsCorrect()
      }
      Row(
        modifier = Modifier.padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        WalletImage(
          data = R.drawable.ic_alert_circle,
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier
          .fillMaxWidth()
          .padding(start = 10.dp)) {
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
private fun ShowPasswordError(shouldShow: Boolean) {
  if (shouldShow) {
    Text(
      text = stringResource(id = R.string.backup_additional_security_password_not_march),
      style = WalletTypography.regular.sp12,
      color = WalletColors.styleguide_red
    )
  }
}

@Preview
@Composable
private fun SwitchModeTruePreview() {
  SwitchModeTrue(
    onPasswordChange = {}, onInputPasswordIsCorrect = {}, onInputPasswordIsIncorrect = {})
}

@Preview
@Composable
fun BalanceCardPreview() {
  BalanceCard("25", "vtyv7uiyh7eybvt3eudbxyuz", "Wallet name", {}, {}, {}, {})
}

@Preview
@Composable
fun BackupEntryButtonPasswordsCorrectPreview() {
  BackupEntryButtonPasswordsCorrect({}, false)
}
