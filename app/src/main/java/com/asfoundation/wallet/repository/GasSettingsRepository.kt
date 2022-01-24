package com.asfoundation.wallet.repository

import android.util.Log
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

class GasSettingsRepository(private val gasService: GasService, private val networkInfo: NetworkInfo) : GasSettingsRepositoryType {

  private var lastFlushTime = 0L
  private var cachedGasPrice: BigDecimal? = null

  override fun getGasSettings(forTokenTransfer: Boolean, multiplier: Double): Single<GasSettings> {
    val web3j = Web3jFactory.build(HttpService(networkInfo.rpcServerUrl))
    return Single.fromCallable {
      var gasPrice = BigDecimal(web3j.ethGasPrice().send().gasPrice)
      gasPrice = gasPrice.multiply(BigDecimal(multiplier)).setScale(0,BigDecimal.ROUND_DOWN)
      Log.d(
        "gas_price",
        " web3j price estimate: " + gasPrice
      )
      return@fromCallable GasSettings(
                        gasPrice,
                        getGasLimit(forTokenTransfer)
                      )
    }
    .subscribeOn(Schedulers.io())

    // gas price using the back-end estimate:
//    return getGasPrice()
//      .map {
//        GasSettings(
//          it.multiply(BigDecimal(multiplier)).setScale(0,BigDecimal.ROUND_DOWN),
//          getGasLimit(forTokenTransfer)
//        )
//      }

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