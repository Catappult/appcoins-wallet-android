package com.asfoundation.wallet.repository

import android.util.Log
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.service.GasService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class GasSettingsRepository(private val gasService: GasService,
                            private val web3jProvider: Web3jProvider,
                            private val rxSchedulers: RxSchedulers
                            ) : GasSettingsRepositoryType {

  private var lastFlushTime = 0L
  private var cachedGasPrice: BigDecimal? = null

  override fun getGasSettings(forTokenTransfer: Boolean, multiplier: Double): Single<GasSettings> {
    return getGasPrice()
      .map {
        val gasPriceMultiplied = it.multiply(BigDecimal(multiplier)).setScale(0,BigDecimal.ROUND_DOWN)
        Log.d(
        "gas_price",
        " web3j price estimate: $it, price used: $gasPriceMultiplied"
        )
        GasSettings(
          gasPriceMultiplied,
          getGasLimit(forTokenTransfer)
        )
      }
  }

  private fun getGasPriceNetwork(): Single<BigDecimal> {
    return Single.fromCallable {
          val gasPrice = web3jProvider.get().ethGasPrice().send().gasPrice
          return@fromCallable BigDecimal(gasPrice)
    }
    .subscribeOn(rxSchedulers.io)

      // gas price using the back-end estimate:
//    return gasService.getGasPrice()
//        .map { BigDecimal(it.price) }
//        .doOnSuccess {
//          cachedGasPrice = it
//          lastFlushTime = System.nanoTime()
//        }
//        .onErrorReturn {
//          if (cachedGasPrice == null) {
//            BigDecimal(DEFAULT_GAS_PRICE)
//          } else {
//            cachedGasPrice
//          }
//        }
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