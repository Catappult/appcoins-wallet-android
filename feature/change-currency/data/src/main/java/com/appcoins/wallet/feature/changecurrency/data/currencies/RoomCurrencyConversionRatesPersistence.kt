package com.appcoins.wallet.feature.changecurrency.data.currencies

import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity.Companion.APPC
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity.Companion.ETH
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity.Companion.ZERO_RATE
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.math.RoundingMode

class RoomCurrencyConversionRatesPersistence(
    private val currencyConversionRatesDao: CurrencyConversionRatesDao
) :
  CurrencyConversionRatesPersistence {

  override fun saveRateFromAppcToFiat(appcValue: String, fiatValue: String, fiatCurrency: String,
                                      fiatSymbol: String): Completable {
    val appcRate = calculateRate(appcValue, fiatValue)
    val entity = CurrencyConversionRateEntity(APPC, fiatCurrency, fiatSymbol, appcRate)
    return saveRate(APPC, entity)
  }

  override fun saveRateFromEthToFiat(ethValue: String, fiatValue: String, fiatCurrency: String,
                                     fiatSymbol: String): Completable {
    val ethRate = calculateRate(ethValue, fiatValue)
    val entity = CurrencyConversionRateEntity(ETH, fiatCurrency, fiatSymbol, ethRate)
    return saveRate(ETH, entity)
  }

  override fun getAppcToLocalFiat(appcValue: String, scale: Int): Single<FiatValue> {
    return currencyConversionRatesDao.getRate(APPC)
        .map { map(it, appcValue, scale) }
  }

  override fun getEthToLocalFiat(ethValue: String, scale: Int): Single<FiatValue> {
    return currencyConversionRatesDao.getRate(ETH)
        .map { map(it, ethValue, scale) }
  }

  private fun map(entity: CurrencyConversionRateEntity, value: String, scale: Int): FiatValue {
    return FiatValue(applyRate(entity.rate, value, scale), entity.fiatCurrency,
        entity.fiatSymbol)
  }

  /**
   * Make sure that the rate being saved does not invalidate what may already be saved in the
   * database. This is particularly relevant for the case where we save an empty rate.
   * If the new rate is non-empty (non-zero), then it will always be saved on DB.
   * If there is a non-zero rate already saved, and the fiat currency is the same, with the new rate
   * being zero, then the new rate will not be saved.
   * If the fiat currency changes (e.g. user starts a VPN connection to a location with a different
   * currency), then the new rate is always saved (even if it is empty).
   * When saving a rate in DB, the existing one will be overwritten
   * @see CurrencyConversionRatesDao.insertRate
   */
  private fun saveRate(currencyFrom: String,
                       newRateEntity: CurrencyConversionRateEntity
  ): Completable {
    return currencyConversionRatesDao.getRate(currencyFrom)
        .map {
          shouldSaveNewRate(it, newRateEntity)
        }
        .onErrorReturn { true }
        .flatMapCompletable { shouldSaveNewRate ->
          if (shouldSaveNewRate) currencyConversionRatesDao.insertRate(newRateEntity)
          else Completable.complete()
        }
  }

  private fun shouldSaveNewRate(oldRate: CurrencyConversionRateEntity,
                                newRate: CurrencyConversionRateEntity
  ): Boolean {
    return bothRatesEmpty(oldRate, newRate) ||
        newRate.rate != ZERO_RATE ||
        oldRate.fiatCurrency != newRate.fiatCurrency
  }

  private fun bothRatesEmpty(oldRate: CurrencyConversionRateEntity,
                             newRate: CurrencyConversionRateEntity
  ): Boolean {
    return oldRate.rate == ZERO_RATE && newRate.rate == ZERO_RATE
  }

  private fun calculateRate(fromValue: String, toValue: String): String {
    val fromValueDecimal = BigDecimal(fromValue)
    val toValueDecimal = BigDecimal(toValue)
    if (fromValueDecimal.compareTo(BigDecimal.ZERO) == 0 || toValueDecimal.compareTo(
        BigDecimal.ZERO
      ) == 0
    ) return ZERO_RATE
    return toValueDecimal.divide(fromValueDecimal, RATE_SCALE, RATE_ROUNDING)
      .toString()
  }

  private fun applyRate(rate: String, value: String, scale: Int): BigDecimal {
    return BigDecimal(rate).multiply(BigDecimal(value))
        .setScale(scale, RATE_ROUNDING)
  }

  companion object {
    private const val RATE_SCALE = 18
    private val RATE_ROUNDING = RoundingMode.FLOOR
  }
}