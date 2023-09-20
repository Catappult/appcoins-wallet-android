package com.appcoins.wallet.feature.personalinfo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.feature.personalinfo.data.CountriesModel
import com.appcoins.wallet.feature.personalinfo.data.PersonalInformation
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.PersonalInfoUiState
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
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
  val countriesUiState = viewModel.countriesUiState.collectAsState().value
  val personalInfoUiState = viewModel.personalInfoUiState.collectAsState().value

  Scaffold(
    topBar = { Surface { TopBar(onClickSupport = { onChatClick() }) } }, modifier = Modifier
  ) { padding ->
    PersonalInformationScreen(
      countriesUiState,
      personalInfoUiState,
      scaffoldPadding = padding
    ) { personalInformation ->
      viewModel.savePersonalInfo(personalInformation)
    }
  }
}

@Composable
fun PersonalInformationScreen(
  countriesUiState: CountriesUiState,
  personalInfoUiState: PersonalInfoUiState,
  scaffoldPadding: PaddingValues,
  onTextFilled: (PersonalInformation) -> Unit
) {
  PersonalInformationList(scaffoldPadding, onTextFilled, countriesUiState, personalInfoUiState)
}

@Composable
private fun PersonalInformationList(
  scaffoldPadding: PaddingValues,
  onTextFilled: (PersonalInformation) -> Unit,
  countriesUiState: CountriesUiState,
  personalInfoUiState: PersonalInfoUiState
) {
  var country by rememberSaveable { mutableStateOf(CountriesModel("", "")) }
  var name by rememberSaveable { mutableStateOf("") }
  var address by rememberSaveable { mutableStateOf("") }
  var city by rememberSaveable { mutableStateOf("") }
  var zipCode by rememberSaveable { mutableStateOf("") }
  var email by rememberSaveable { mutableStateOf("") }
  var fiscalId by rememberSaveable { mutableStateOf("") }

  LazyColumn(
    modifier =
    Modifier
      .fillMaxSize()
      .padding(top = scaffoldPadding.calculateTopPadding())
      .padding(16.dp)
  ) {
    item {
      Text(
        text = stringResource(R.string.settings_personal_title),
        style = WalletTypography.bold.sp22,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (countriesUiState) {
          is CountriesUiState.Success ->
            DropDownMenu(countries = countriesUiState.countries) { selectedCountry ->
              country = selectedCountry
            }

          CountriesUiState.ApiError, CountriesUiState.Idle -> Unit
        }
        when (personalInfoUiState) {
          PersonalInfoUiState.ApiError, PersonalInfoUiState.Idle -> Unit
          is PersonalInfoUiState.Success -> {
            WalletTextFieldCustom(value = name, title = "Name and surname") { name = it }
            WalletTextFieldCustom(value = address, title = "Address") { address = it }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              WalletTextFieldCustom(
                value = city,
                title = "City",
                modifier = Modifier
                  .fillMaxWidth(0.5f)
                  .padding(end = 8.dp)
              ) {
                city = it
              }
              WalletTextFieldCustom(value = zipCode, title = "Zip Code") { zipCode = it }
            }
            WalletTextFieldCustom(
              value = email, title = "E-mail", keyboardType = KeyboardType.Email
            ) {
              email = it
            }
            WalletTextFieldCustom(
              value = fiscalId,
              title = "VAT number/Fiscal ID",
            ) {
              fiscalId = it
            }
          }
        }
      }


    }

    item {
      Column(modifier = Modifier.padding(top = 40.dp, bottom = 8.dp)) {
        ButtonWithText(
          label = stringResource(R.string.action_save),
          onClick = {
            onTextFilled(
              PersonalInformation(country, name, address, city, zipCode, email, fiscalId)
            )
          },
          labelColor = styleguide_light_grey,
          backgroundColor = WalletColors.styleguide_pink,
          buttonType = ButtonType.LARGE
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(countries: List<CountriesModel>, onCountryChanged: (CountriesModel) -> Unit) {
  var expanded by rememberSaveable { mutableStateOf(false) }
  var selectedCountry by rememberSaveable { mutableStateOf(CountriesModel("", "")) }
  onCountryChanged(selectedCountry)

  ExposedDropdownMenuBox(
    expanded = expanded,
    { expanded = !expanded },
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 32.dp)
  ) {
    TextField(
      value = selectedCountry.translatedName,
      onValueChange = {},
      readOnly = true,
      shape = RoundedCornerShape(8.dp),
      colors =
      TextFieldDefaults.colors(
        unfocusedContainerColor = styleguide_blue_secondary,
        unfocusedIndicatorColor = styleguide_blue,
        unfocusedTextColor = styleguide_light_grey,
      ),
      trailingIcon = {
        Icon(
          Icons.Filled.KeyboardArrowDown,
          tint = WalletColors.styleguide_pink,
          modifier = Modifier.rotate(if (expanded) 180f else 0f),
          contentDescription = null
        )
      },
      placeholder = { Text(text = "Select your country", color = styleguide_dark_grey) },
      modifier = Modifier
        .fillMaxWidth()
        .menuAnchor()
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier =
      Modifier
        .exposedDropdownSize()
        .background(color = styleguide_blue_secondary)
        .padding(horizontal = 8.dp)
    ) {
      countries.forEach { country ->
        DropdownMenuItem(
          text = {
            Text(
              text = country.translatedName,
              color =
              if (selectedCountry == country) styleguide_light_grey
              else styleguide_dark_grey,
              modifier = Modifier.padding(start = 16.dp),
            )
          },
          onClick = {
            selectedCountry = country
            expanded = false
          },
          modifier =
          Modifier.background(
            color =
            if (selectedCountry == country) styleguide_blue
            else styleguide_blue_secondary,
            shape = RoundedCornerShape(8.dp)
          ),
          trailingIcon = {
            if ((selectedCountry == country))
              Icon(
                Icons.Filled.Done,
                tint = WalletColors.styleguide_pink,
                contentDescription = "selected"
              )
          })
      }
    }
  }
}
