package com.appcoins.wallet.feature.changecurrency.ui.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.WalletImage
import com.appcoins.wallet.ui.widgets.component.ButtonWithText

@Composable
internal fun ChooseCurrencyRoute(
  chosenCurrency: FiatCurrency?,
  viewModel: ChooseCurrencyBottomSheetViewModel = hiltViewModel(),
  bottomSheetStateHandle: () -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
) {
  ChooseCurrencyScreen(
    chosenCurrency = chosenCurrency,
    currencyConfirmationClick = viewModel::currencyConfirmationClick,
    fragmentName = fragmentName,
    buttonsAnalytics = buttonsAnalytics
  )
  val state by viewModel.stateFlow.collectAsState()
  when (state.selectedConfirmationAsync) {
    Async.Uninitialized,
    is Async.Loading -> Unit

    is Async.Success -> {
      bottomSheetStateHandle()
    }

    is Async.Fail -> {
    }
  }
}

@Composable
fun ChooseCurrencyScreen(
  chosenCurrency: FiatCurrency?,
  currencyConfirmationClick: (String, String?) -> Unit,
  fragmentName: String,
  buttonsAnalytics: ButtonsAnalytics?
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
      style = WalletTypography.medium.sp22,
      modifier = Modifier.padding(top = 8.dp)
    )
    Text(
      text = chosenCurrency.label!!,
      style = WalletTypography.medium.sp16,
      color = WalletColors.styleguide_medium_grey,
      modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    ButtonWithText(
      label = stringResource(id = R.string.confirm_button),
      onClick = {
        currencyConfirmationClick(chosenCurrency.currency, chosenCurrency.sign)
      },
      backgroundColor = WalletColors.styleguide_primary,
      labelColor = MaterialTheme.colorScheme.primaryContainer,
      fragmentName = fragmentName,
      buttonsAnalytics = buttonsAnalytics
    )
  }
}

@Preview("ChooseCurrency")
@Composable
private fun ChooseCurrencyPreview() {
  ChooseCurrencyScreen(
    chosenCurrency = FiatCurrency(
      "EUR",
      label = "Euro",
      flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
      sign = "€"
    ),
    currencyConfirmationClick = { a,b -> },
    fragmentName = "HomeFragment",
    buttonsAnalytics = null
  )
}