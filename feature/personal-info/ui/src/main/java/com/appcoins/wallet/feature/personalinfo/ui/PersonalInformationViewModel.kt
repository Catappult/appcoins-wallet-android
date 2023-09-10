package com.appcoins.wallet.feature.personalinfo.ui

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState.Idle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PersonalInformationViewModel @Inject constructor() : ViewModel() {
  private val _countriesUiState = MutableStateFlow<CountriesUiState>(Idle)
  val countriesUiState: StateFlow<CountriesUiState> = _countriesUiState

  sealed class CountriesUiState {
    object Idle : CountriesUiState()
    data class Success(val countries: List<String>) : CountriesUiState()
  }
}