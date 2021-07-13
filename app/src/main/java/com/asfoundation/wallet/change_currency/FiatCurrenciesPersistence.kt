package com.asfoundation.wallet.change_currency

import io.reactivex.Completable
import io.reactivex.Single

interface FiatCurrenciesPersistence {

  fun getFiatCurrency(currency: String): Single<FiatCurrency>

  fun getFiatCurrencies(): Single<List<FiatCurrency>>

  fun saveCurrency(autoUploadSelects: FiatCurrency): Completable

  fun replaceAllBy(list: List<FiatCurrency>): Completable

}