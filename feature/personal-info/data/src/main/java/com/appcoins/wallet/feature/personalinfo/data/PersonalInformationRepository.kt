package com.appcoins.wallet.feature.personalinfo.data

import androidx.compose.ui.text.intl.Locale
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.CountriesResponse
import com.appcoins.wallet.core.network.base.call_adapter.Result
import com.appcoins.wallet.core.network.base.call_adapter.handleApi
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface PersonalInformationRepository {
  fun getCountries(): Flow<Result<List<CountriesResponse>>>
}

@BoundTo(supertype = PersonalInformationRepository::class)
class PersonalInformationRepositoryDefault @Inject constructor(private val api: TransactionsApi) :
  PersonalInformationRepository {
  override fun getCountries(): Flow<Result<List<CountriesResponse>>> {
    return flow {
      emit(handleApi { api.getCountriesByLanguage(languageCode = Locale.current.language) })
    }.flowOn(Dispatchers.IO)
  }
}