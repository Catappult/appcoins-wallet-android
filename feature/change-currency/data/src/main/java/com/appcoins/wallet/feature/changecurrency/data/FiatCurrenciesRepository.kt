package com.appcoins.wallet.feature.changecurrency.data

import android.content.Context
import com.appcoins.wallet.core.arch.data.DataResult
import com.appcoins.wallet.core.arch.data.toDataResult
import com.appcoins.wallet.core.network.microservices.api.broker.FiatCurrenciesApi
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiatCurrenciesRepository @Inject constructor(
  private val fiatCurrenciesApi: FiatCurrenciesApi,
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource,
  private val fiatCurrenciesDao: FiatCurrenciesDao,
  private val conversionService: LocalCurrencyConversionService,
  private val dispatchers: Dispatchers,
  @ApplicationContext private val context: Context
) {

  private suspend fun fetchCurrenciesList(): DataResult<List<FiatCurrency>> =
    withContext(dispatchers.io) {
      fiatCurrenciesApi.getFiatCurrencies()
        .map { response ->
          response.mapResponseToCurrencyListEntity()
        }
        .onSuccess {
          fiatCurrenciesDao.replaceAllBy(it)
        }
        .map {
          it.mapToCurrency()
        }
    }

  suspend fun getCurrenciesList(): DataResult<List<FiatCurrency>> =
    withContext(dispatchers.io) {
      val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
      if (fiatCurrenciesPreferencesDataSource.getCurrencyListLastVersion() != versionCode ||
        fiatCurrenciesDao.getFiatCurrencies().isEmpty()
      ) {
        fiatCurrenciesPreferencesDataSource.setCurrencyListLastVersion(versionCode)
        return@withContext fetchCurrenciesList()
      } else {
        fiatCurrenciesDao.getFiatCurrencies().mapToCurrency().toDataResult()
      }
    }

  suspend fun getSelectedCurrency(): DataResult<String> {
    return if (fiatCurrenciesPreferencesDataSource.getSelectCurrency()) {
      val fiatValue: FiatValue = conversionService.localCurrency.await()
      fiatCurrenciesPreferencesDataSource.setSelectedCurrency(fiatValue.currency)
      fiatCurrenciesPreferencesDataSource.setSelectedCurrencySymbol(fiatValue.symbol)
      fiatCurrenciesPreferencesDataSource.setSelectFirstTime()
      fiatCurrenciesPreferencesDataSource.setSelectFirstTimeSymbol()
      fiatValue.currency.toDataResult()
    } else {
      getCachedResultSelectedCurrency()
    }
  }

  suspend fun getSelectedCurrencySymbol(): DataResult<String> {
    return if (fiatCurrenciesPreferencesDataSource.getSelectCurrencySymbol()) {
      val fiatValue: FiatValue = conversionService.localCurrency.await()
      fiatCurrenciesPreferencesDataSource.setSelectedCurrency(fiatValue.currency)
      fiatCurrenciesPreferencesDataSource.setSelectedCurrencySymbol(fiatValue.symbol)
      fiatCurrenciesPreferencesDataSource.setSelectFirstTimeSymbol()
      fiatCurrenciesPreferencesDataSource.setSelectFirstTime()
      fiatValue.symbol.toDataResult()
    } else {
      getCachedResultSelectedCurrencySymbol()
    }
  }

  fun getCachedResultSelectedCurrency(): DataResult<String> {
    return fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency().toDataResult()
  }

  fun getCachedResultSelectedCurrencySymbol(): DataResult<String> {
    return fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrencySymbol().toDataResult()
  }

  fun getCachedSelectedCurrency(): String? =
    fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrency()

  fun getCachedSelectedCurrencySymbol(): String? =
    fiatCurrenciesPreferencesDataSource.getCachedSelectedCurrencySymbol()


  suspend fun setSelectedCurrency(currency: String) {
    withContext(dispatchers.io) {
      fiatCurrenciesPreferencesDataSource.setSelectedCurrency(currency)
    }
  }

  suspend fun setSelectedCurrencySymbol(symbol: String) {
    withContext(dispatchers.io) {
      fiatCurrenciesPreferencesDataSource.setSelectedCurrencySymbol(symbol)
    }
  }
}