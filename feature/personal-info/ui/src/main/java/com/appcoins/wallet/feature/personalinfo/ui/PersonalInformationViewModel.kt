package com.appcoins.wallet.feature.personalinfo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.base.call_adapter.ApiException
import com.appcoins.wallet.core.network.base.call_adapter.ApiFailure
import com.appcoins.wallet.core.network.base.call_adapter.ApiSuccess
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.personalinfo.data.CountriesModel
import com.appcoins.wallet.feature.personalinfo.data.PersonalInformation
import com.appcoins.wallet.feature.personalinfo.data.mapToModel
import com.appcoins.wallet.feature.personalinfo.data.usecases.GetCountriesUseCase
import com.appcoins.wallet.feature.personalinfo.data.usecases.GetPersonalInfoUseCase
import com.appcoins.wallet.feature.personalinfo.data.usecases.SavePersonalInfoUseCase
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
class PersonalInformationViewModel
@Inject
constructor(
  private val getCountriesUseCase: GetCountriesUseCase,
  private val savePersonalInfoUseCase: SavePersonalInfoUseCase,
  private val getPersonalInfoUseCase: GetPersonalInfoUseCase,
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val logger: Logger
) : ViewModel() {
  private val tag = PersonalInformationViewModel::class.java.name

  private val _countriesUiState = MutableStateFlow<CountriesUiState>(Idle)
  val countriesUiState: StateFlow<CountriesUiState> = _countriesUiState

  private val _personalInfoUiState = MutableStateFlow<PersonalInfoUiState>(PersonalInfoUiState.Idle)
  val personalInfoUiState: StateFlow<PersonalInfoUiState> = _personalInfoUiState


  init {
    getCountries()
    getPersonalInfo()
  }

  fun savePersonalInfo(personalInformation: PersonalInformation) {
    runWithEwt { ewt ->
      viewModelScope.launch {
        savePersonalInfoUseCase(ewt, personalInformation.mapToRequest())
          .catch { logger.log(tag, it) }
          .collect { result ->
            when (result) {
              is ApiSuccess -> {
                Log.d("giovanniteste", result.data.message)
              }

              is ApiException -> {
                logger.log(tag, result.e)
              }

              is ApiFailure -> {
                logger.log(tag, "${result.code}  ${result.message}")
              }
            }
          }
      }
    }
  }

  private fun getPersonalInfo() {
    runWithEwt { ewt ->
      viewModelScope.launch {
        getPersonalInfoUseCase(ewt)
          .catch { logger.log(tag, it) }
          .collect { result ->
            when (result) {
              is ApiSuccess -> {
                _personalInfoUiState.value =
                  PersonalInfoUiState.Success(result.data.mapToModel())
              }

              is ApiException -> {
                logger.log(tag, result.e)
              }

              is ApiFailure -> {
                logger.log(tag, "${result.code}  ${result.message}")
              }
            }
          }
      }
    }
  }

  private fun getCountries() {
    viewModelScope.launch {
      getCountriesUseCase()
        .catch { logger.log(tag, it) }
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              _countriesUiState.value =
                Success(result.data.map { CountriesModel(it.name, it.translatedName) })
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

  private fun runWithEwt(invoke: (String) -> Unit) {
    ewtAuthenticatorService
      .getEwtAuthentication()
      .doOnSuccess { ewt ->
        Log.d("giovanniteste", ewt)
        invoke(ewt)
      }.subscribe()
  }

  sealed class CountriesUiState {
    object Idle : CountriesUiState()
    object ApiError : CountriesUiState()
    data class Success(val countries: List<CountriesModel>) : CountriesUiState()
  }

  sealed class PersonalInfoUiState {
    object Idle : PersonalInfoUiState()
    object ApiError : PersonalInfoUiState()
    data class Success(val personalInfo: PersonalInformation) : PersonalInfoUiState()
  }
}
