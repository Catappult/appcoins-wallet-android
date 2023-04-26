package com.appcoins.wallet.feature.changecurrency.data

import android.content.Context
import com.appcoins.wallet.core.network.microservices.api.broker.FiatCurrenciesApi
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import com.appcoins.wallet.ui.arch.data.DataResult
import com.appcoins.wallet.ui.arch.data.toDataResult
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: FiatCurrenciesApi,
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource,
  private val fiatCurrenciesMapper: FiatCurrenciesMapper,
  private val fiatCurrenciesDao: FiatCurrenciesDao,
  private val conversionService: LocalCurrencyConversionService,
  private val dispatchers: Dispatchers,
  @ApplicationContext private val context: Context
) {

  private suspend fun fetchCurrenciesList(): DataResult<List<FiatCurrencyEntity>> =
    withContext(dispatchers.io) {
      fiatCurrenciesApi.getFiatCurrencies()
        .map { response ->
          fiatCurrenciesMapper.mapResponseToCurrencyList(response)
        }
        .onSuccess {
          fiatCurrenciesDao.replaceAllBy(it)
        }
    }


  suspend fun getCurrenciesList(): DataResult<List<FiatCurrencyEntity>> =
    withContext(dispatchers.io) {
      val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
      if (fiatCurrenciesPreferencesDataSource.getCurrencyListLastVersion() != versionCode) {
        fiatCurrenciesPreferencesDataSource.setCurrencyListLastVersion(versionCode)
        return@withContext fetchCurrenciesList()
      } else {
        fiatCurrenciesDao.getFiatCurrencies().toDataResult()
      }
    }

  suspend fun getSelectedCurrency(): DataResult<String> {
    return if (fiatCurrenciesPreferencesDataSource.getSelectCurrency()) {
      val fiatValue: FiatValue = conversionService.localCurrency.await()
      fiatCurrenciesPreferencesDataSource.setSelectedCurrency(fiatValue.currency)
      fiatCurrenciesPreferencesDataSource.setSelectFirstTime()
      fiatValue.currency.toDataResult()
    } else {
      getCachedSelectedCurrency()
    }
  }

  fun getCachedSelectedCurrency(): DataResult<String> {
    return fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency().toDataResult()
  }

  fun setSelectedCurrency(currency: String) {
    fiatCurrenciesPreferencesDataSource.setSelectedCurrency(currency)
  }
}