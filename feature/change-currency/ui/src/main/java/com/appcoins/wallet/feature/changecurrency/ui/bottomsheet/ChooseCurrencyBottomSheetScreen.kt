package com.appcoins.wallet.feature.changecurrency.ui.bottomsheet

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.core.utils.jvm_common.DevUtils.CUSTOM_TAG
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrencyEntity

@Composable
internal fun ChooseCurrencyRoute(
  chosenCurrency: FiatCurrencyEntity?,
  viewModel: ChooseCurrencyBottomSheetViewModel = hiltViewModel(),
  onExitClick: () -> Unit
) {
  ChooseCurrencyScreen(
    chosenCurrency = chosenCurrency,
    currencyConfirmationClick = viewModel::currencyConfirmationClick
  )
  val state by viewModel.stateFlow.collectAsState()
  when (state.selectedConfirmationAsync) {
    Async.Uninitialized,
    is Async.Loading -> Unit
    is Async.Success -> {
      onExitClick()
      Log.d(CUSTOM_TAG, ": ChooseCurrencyScreen: confirm success")
    }
    is Async.Fail -> {
      Log.d(CUSTOM_TAG, ": ChooseCurrencyScreen: fail ")
    }
  }
}

@Composable
private fun ChooseCurrencyScreen(
  chosenCurrency: FiatCurrencyEntity?,
  currencyConfirmationClick: (String) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    WalletImage(
      data = chosenCurrency?.flag,
      modifier = Modifier
        .clip(CircleShape)
        .size(60.dp)
    )
    Text(
      text = chosenCurrency?.currency!!,
      style = WalletTypography.medium.L,
      modifier = Modifier.padding(top = 8.dp)
    )
    Text(
      text = chosenCurrency.label!!,
      style = WalletTypography.medium.M,
      color = WalletColors.styleguide_medium_grey,
      modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    ButtonWithText(
      label = R.string.confirm_button,
      onClick = { currencyConfirmationClick(chosenCurrency.currency) },
      backgroundColor = WalletColors.styleguide_pink,
      labelColor = WalletColors.styleguide_white
    )
  }
}

@Preview("ChooseCurrency")
@Composable
private fun ChooseCurrencyPreview() {
  ChooseCurrencyScreen(
    chosenCurrency = FiatCurrencyEntity(
      "EUR",
      label = "Euro",
      flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
      sign = "€"
    ),
    currencyConfirmationClick = {}
  )
}