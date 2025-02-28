package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextFieldCustom

lateinit var emailInput: String

@Composable
fun BackupSaveOptionsRoute(
  onChatClick: () -> Unit,
  onSaveOnDevice: () -> Unit,
  viewModel: BackupSaveOptionsViewModel = hiltViewModel(),
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  Scaffold(
    topBar = { Surface { TopBar(onClickSupport = onChatClick, fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics) } },
    modifier = Modifier
  ) { padding ->
    BackupSaveOptionsScreen(
      scaffoldPadding = padding, viewModel.showLoading.value, onSaveOnDevice = onSaveOnDevice, fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics
    ) { isEmailCommunicationSelected ->
      viewModel.showLoading()
      viewModel.sendBackupToEmail(emailInput)
      if (isEmailCommunicationSelected) viewModel.postUserEmailCommunication(emailInput)
    }
  }
}

@Composable
fun BackupSaveOptionsScreen(
  scaffoldPadding: PaddingValues,
  showLoading: Boolean,
  onSaveOnDevice: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?,
  onSendEmailClick: (isEmailCommunicationSelected: Boolean) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize(1f)
      .padding(scaffoldPadding)
      .verticalScroll(
        rememberScrollState(),
      )
  ) {
    Column(horizontalAlignment = Alignment.Start) {
      Text(
        style = WalletTypography.bold.sp22,
        color = WalletColors.styleguide_light_grey,
        text = stringResource(id = R.string.backup_title),
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 24.dp),
      )
      Text(
        text = stringResource(id = R.string.backup_body),
        modifier = Modifier.padding(horizontal = 24.dp),
        style = WalletTypography.medium.sp14,
        color = WalletColors.styleguide_light_grey,
      )
    }
    if (showLoading) {
      LoadingCard()
    } else {
      SaveOnDeviceCardDefault()
      SaveOnDeviceOptions(
        onSendEmailClick,
        onSaveOnDevice = onSaveOnDevice,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }
}

@Preview
@Composable
fun SaveOnDeviceCardDefault() {
  Card(
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp, horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = styleguide_dark_secondary)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)
    ) {
      WalletImage(Modifier.size(48.dp, 62.dp), data = R.drawable.ic_lock_appc)
      Text(
        text = stringResource(id = R.string.backup_ready_title),
        color = WalletColors.styleguide_light_grey,
        style = WalletTypography.bold.sp22,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
      )
    }
  }
}

@Preview
@Composable
fun LoadingCard() {
  Card(
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp, horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = styleguide_dark_secondary)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 48.dp)
    ) {
      val composition by
      rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_wallet))
      val progress by
      animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

      LottieAnimation(
        modifier = Modifier.size(80.dp),
        composition = composition,
        progress = { progress })

      Text(
        text = stringResource(id = R.string.title_dialog_sending),
        color = styleguide_dark_grey,
        style = WalletTypography.medium.sp12,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun SaveOnDeviceOptions(
  onSendEmailClick: (isEmailCommunicationSelected: Boolean) -> Unit,
  onSaveOnDevice: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  var defaultEmail by rememberSaveable { mutableStateOf("") }
  var validEmail by rememberSaveable { mutableStateOf(false) }
  val checkedCommunicationState = remember { mutableStateOf(false) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Column(
        Modifier.padding(start = 24.dp, bottom = 2.dp, end = 24.dp, top = 24.dp),
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
            emailInput = defaultEmail
          },
          hintText = R.string.email_here_field,
        )
        if (defaultEmail.isNotEmpty() &&
          android.util.Patterns.EMAIL_ADDRESS.matcher(defaultEmail).matches()
        ) {
          validEmail = true
        }

      }
      CheckboxCommunicationEmail(
        checked = checkedCommunicationState.value,
        startPadding = 12.dp,
        endPadding = 24.dp,
        onCheckedChange = { checkedCommunicationState.value = it }
      )
    }

    Column {
      Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)) {
        ButtonWithText(
          label = stringResource(id = R.string.backup_ready_email_button),
          onClick = { if (validEmail) onSendEmailClick(checkedCommunicationState.value) },
          backgroundColor =
          if (validEmail) WalletColors.styleguide_primary else styleguide_dark_grey,
          labelColor = WalletColors.styleguide_light_grey,
          buttonType = ButtonType.LARGE,
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 1.25.dp, bottom = 8.75.dp)
      ) {
        Divider(
          modifier = Modifier
            .weight(1f)
            .height(1.dp), color = styleguide_dark_grey
        )
        Text(
          text = stringResource(R.string.common_or),
          color = styleguide_dark_grey,
          style = WalletTypography.regular.sp12,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
          modifier = Modifier
            .weight(1f)
            .height(1.dp), color = styleguide_dark_grey
        )
      }
      Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 27.dp)) {
        ButtonWithText(
          label = stringResource(id = R.string.backup_ready_device_button),
          onClick = { onSaveOnDevice() },
          labelColor = WalletColors.styleguide_white,
          buttonType = ButtonType.LARGE,
          outlineColor = WalletColors.styleguide_light_grey,
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      }
    }
  }
}

@Composable
fun CheckboxCommunicationEmail(
  checked: Boolean,
  startPadding: Dp,
  endPadding: Dp,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.padding(bottom = 16.dp, start = startPadding, end = endPadding)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = WalletColors.styleguide_primary,
        uncheckedColor = WalletColors.styleguide_dark_grey,
        checkmarkColor = WalletColors.styleguide_white,
      )
    )
    Text(
      text = stringResource(id = R.string.mail_list_card_body),
      style = WalletTypography.medium.sp12,
      color = WalletColors.styleguide_dark_grey,
      modifier = Modifier
        .padding(start = 0.dp, top = 8.dp)
    )
  }
}

@Preview
@Composable
fun BackupSaveOptionsScreenPreview() {
  BackupSaveOptionsScreen(PaddingValues(0.dp), false, {}, "HomeFragment", ButtonsAnalytics(null)) {}
}
