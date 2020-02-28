package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.service.GasService
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class GasSettingsRepository(private val gasService: GasService) : GasSettingsRepositoryType {

  private var cachedGasPrice: BigDecimal? = null

  private fun getCachedGasPrice(): Observable<BigDecimal> {
    return Observable.create<BigDecimal> {
      if (cachedGasPrice != null) {
        it.onNext(cachedGasPrice!!)
      }
      it.onComplete()
    }
  }

  private fun getCachedGasPriceNetwork(): Observable<BigDecimal> {
    return gasService.getGasPrice()
        .map { BigDecimal(it.price) }
        .onErrorReturn { BigDecimal(DEFAULT_GAS_PRICE) }
        .toObservable()
  }

  override fun getGasSettings(forTokenTransfer: Boolean): Single<GasSettings> {
    return Observable.concat(getCachedGasPrice(), getCachedGasPriceNetwork())
        .firstElement()
        .toObservable()
        .map { GasSettings(it, getGasLimit(forTokenTransfer)) }
        .firstOrError()
  }

  private fun getGasLimit(forTokenTransfer: Boolean): BigDecimal {
    return BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS).takeIf { forTokenTransfer } ?: BigDecimal(
        DEFAULT_GAS_LIMIT)
  }

  companion object {
    const val DEFAULT_GAS_LIMIT = "90000"
    const val DEFAULT_GAS_LIMIT_FOR_TOKENS = "144000"
    const val DEFAULT_GAS_PRICE = "30000000000"
  }


}