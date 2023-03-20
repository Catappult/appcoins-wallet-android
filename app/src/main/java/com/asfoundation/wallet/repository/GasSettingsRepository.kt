package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.GasService
import com.asfoundation.wallet.entity.GasSettings
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@BoundTo(supertype = GasSettingsRepositoryType::class)
class GasSettingsRepository @Inject constructor(private val gasService: GasService) :
    GasSettingsRepositoryType {

  private var lastFlushTime = 0L
  private var cachedGasPrice: BigDecimal? = null

  override fun getGasSettings(forTokenTransfer: Boolean): Single<GasSettings> {
    return getGasPrice()
        .map { GasSettings(it, getGasLimit(forTokenTransfer)) }
  }

  private fun getGasPriceNetwork(): Single<BigDecimal> {
    return gasService.getGasPrice()
        .map { BigDecimal(it.price) }
        .doOnSuccess {
          cachedGasPrice = it
          lastFlushTime = System.nanoTime()
        }
        .onErrorReturn {
          if (cachedGasPrice == null) {
            BigDecimal(DEFAULT_GAS_PRICE)
          } else {
            cachedGasPrice
          }
        }
  }

  private fun shouldRefresh() =
      System.nanoTime() - lastFlushTime >= TimeUnit.MINUTES.toNanos(1) || cachedGasPrice == null

  private fun getGasPriceLocal(): Single<BigDecimal> = Single.just(cachedGasPrice)

  private fun getGasPrice(): Single<BigDecimal> {
    return if (shouldRefresh()) {
      getGasPriceNetwork()
    } else {
      getGasPriceLocal()
    }
  }

  private fun getGasLimit(forTokenTransfer: Boolean): BigDecimal {
    return if (forTokenTransfer) {
      BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS)
    } else {
      BigDecimal(DEFAULT_GAS_LIMIT)
    }
  }

  companion object {
    const val DEFAULT_GAS_LIMIT = "90000"
    const val DEFAULT_GAS_LIMIT_FOR_TOKENS = "144000"
    const val DEFAULT_GAS_PRICE = "30000000000"
  }

}