package com.appcoins.wallet.feature.personalinfo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.base.call_adapter.ApiException
import com.appcoins.wallet.core.network.base.call_adapter.ApiFailure
import com.appcoins.wallet.core.network.base.call_adapter.ApiSuccess
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.personalinfo.data.CountriesModel
import com.appcoins.wallet.feature.personalinfo.data.PersonalInformation
import com.appcoins.wallet.feature.personalinfo.data.usecases.GetCountriesUseCase
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState.ApiError
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState.Idle
import com.appcoins.wallet.feature.personalinfo.ui.PersonalInformationViewModel.CountriesUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInformationViewModel @Inject constructor(
  private val getCountriesUseCase: GetCountriesUseCase,
  private val logger: Logger
) : ViewModel() {
  private val tag = PersonalInformationViewModel::class.java.name


  private val _countriesUiState = MutableStateFlow<CountriesUiState>(Idle)
  val countriesUiState: StateFlow<CountriesUiState> = _countriesUiState

  init {
    getCountries()
  }

  fun saveInfo(personalInformation: PersonalInformation) {

  }

  private fun getCountries() {
    viewModelScope.launch {
      getCountriesUseCase()
        .catch { logger.log(tag, it) }
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              _countriesUiState.value = Success(result.data.map {
                CountriesModel(it.name, it.translatedName)
              })
            }

            is ApiException -> {
              _countriesUiState.value = ApiError
              logger.log(tag, result.e)
            }

            is ApiFailure -> {
              _countriesUiState.value = ApiError
              logger.log(tag, "${result.code}  ${result.message}")
            }
          }
        }
    }
  }

  sealed class CountriesUiState {
    object Idle : CountriesUiState()
    object ApiError : CountriesUiState()
    data class Success(val countries: List<CountriesModel>) : CountriesUiState()
  }
}