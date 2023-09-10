package com.appcoins.wallet.feature.personalinfo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextFieldCustom

@Composable
fun PersonalInformationRoute(
  onChatClick: () -> Unit,
  viewModel: PersonalInformationViewModel = hiltViewModel()
) {
  val countriesUiState = viewModel.countriesUiState.collectAsState()
  Scaffold(
    topBar = { Surface { TopBar(onClickSupport = { onChatClick() }) } }, modifier = Modifier
  ) { padding ->
    PersonalInformationScreen(countriesUiState, scaffoldPadding = padding)
  }
}

@Composable
fun PersonalInformationScreen(
  countriesUiState: State<CountriesUiState>,
  scaffoldPadding: PaddingValues
) {
  PersonalInformationList(scaffoldPadding)
}

@Composable
private fun PersonalInformationList(scaffoldPadding: PaddingValues) {
  var nameValue by rememberSaveable { mutableStateOf("") }

  LazyColumn(
    modifier =
    Modifier
      .fillMaxSize()
      .padding(top = scaffoldPadding.calculateTopPadding())
      .padding(horizontal = 16.dp)
  ) {
    item {
      Text(
        text = stringResource(R.string.settings_personal_title),
        style = WalletTypography.bold.sp22,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WalletTextFieldCustom(value = nameValue, title = "Name and surname") { nameValue = it }
        WalletTextFieldCustom(value = nameValue, title = "Name and surname") { nameValue = it }
        Row() {
          WalletTextFieldCustom(value = nameValue, title = "Name and surname") {
            nameValue = it
          }
          WalletTextFieldCustom(value = nameValue, title = "Name and surname") {
            nameValue = it
          }
        }
        WalletTextFieldCustom(value = nameValue, title = "Name and surname") { nameValue = it }
        WalletTextFieldCustom(value = nameValue, title = "Nae and surname") { nameValue = it }
      }
    }

    item {
      ButtonWithText(
        label = stringResource(R.string.action_save),
        onClick = { /*TODO*/ },
        labelColor = WalletColors.styleguide_light_grey,
        backgroundColor = WalletColors.styleguide_pink,
        buttonType = ButtonType.LARGE
      )
    }
  }
}
