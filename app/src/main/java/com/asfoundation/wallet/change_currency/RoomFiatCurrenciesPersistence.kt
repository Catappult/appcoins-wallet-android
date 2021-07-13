package com.asfoundation.wallet.change_currency

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class RoomFiatCurrenciesPersistence(private val fiatCurrenciesDao: FiatCurrenciesDao) :
    FiatCurrenciesPersistence {


  override fun getFiatCurrency(currency: String): Single<FiatCurrency> {
    return fiatCurrenciesDao.getFiatCurrency(currency)
        .subscribeOn(Schedulers.io())
  }

  override fun getFiatCurrencies(): Single<List<FiatCurrency>> {
    return fiatCurrenciesDao.getFiatCurrencies()
        .subscribeOn(Schedulers.io())
  }

  override fun saveCurrency(fiatCurrency: FiatCurrency): Completable {
    return fiatCurrenciesDao.saveCurrency(fiatCurrency)
        .subscribeOn(Schedulers.io())
  }

  override fun replaceAllBy(list: List<FiatCurrency>): Completable {
    return Completable.fromAction {
      fiatCurrenciesDao.removeAll()
      fiatCurrenciesDao.saveAll(list)
    }
        .subscribeOn(Schedulers.io())
  }
}