package com.asfoundation.wallet.wallets.repository

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.AppcoinsBalanceRepository
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.BalanceUtils
import com.asfoundation.wallet.wallets.db.WalletInfoDao
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal
import java.math.RoundingMode

class WalletInfoRepository(
  private val api: WalletInfoApi,
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val walletInfoDao: WalletInfoDao,
  private val localCurrencyConversionService: LocalCurrencyConversionService,
  private val defaultNetwork: NetworkInfo,
  private val rxSchedulers: RxSchedulers
) {

  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"

    private const val FIAT_SCALE = 4
  }

  fun observeWalletInfo(walletAddress: String): Observable<WalletInfo> {
    return walletInfoDao.observeWalletInfo(walletAddress)
      .map { list ->
        if (list.isNotEmpty()) {
          return@map WalletInfo(
            list[0].wallet, list[0].ethBalanceWei, list[0].appcBalanceWei,
            list[0].appcCreditsBalanceWei, list[0].blocked, list[0].verified, list[0].logging
          )
        }
        throw UnknownError("No wallet info for the specified wallet address (${walletAddress})")
      }
      .subscribeOn(rxSchedulers.io)
  }

  fun updateWalletInfo(walletAddress: String, updateFiat: Boolean): Completable {
    return api.getWalletInfo(walletAddress)
      .flatMap { walletInfoResponse ->
        if (updateFiat) {

        }
        return@flatMap Single.just(
          WalletInfoEntity(
            walletInfoResponse.wallet, walletInfoResponse.ethBalanceWei,
            walletInfoResponse.appcBalanceWei, walletInfoResponse.appcCreditsBalanceWei,
            walletInfoResponse.blocked, walletInfoResponse.verified, walletInfoResponse.logging,
            null, null, null, null, null
          )
        )
      }
      .map { response ->

      }
      .doOnSuccess { entity -> walletInfoDao.insertWalletInfo(entity) }
      .ignoreElement()
      .subscribeOn(rxSchedulers.io)
  }

  private fun convertToFiat(
    appcCreditsWei: BigDecimal,
    appcWei: BigDecimal,
    ethWei: BigDecimal
  ): WalletBalance {
    val appcCredits = BalanceUtils.weiToEth(appcCreditsWei).setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val appc = BalanceUtils.weiToEth(appcWei).setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val eth = BalanceUtils.weiToEth(ethWei).setScale(FIAT_SCALE, RoundingMode.FLOOR)

    localCurrencyConversionService.getValueToFiat(
      appcCredits.toString(), "ETH",
      targetCurrency, AppcoinsBalanceRepository.SUM_FIAT_SCALE
    )
    return getSelectedCurrencyUseCase(bypass = false)
      .flatMap { targetCurrency ->
        Single.zip(
          localCurrencyConversionService.getValueToFiat(
            appcCredits.toString(),
            "APPC",
            targetCurrency,
            FIAT_SCALE
          ),
          localCurrencyConversionService.getValueToFiat(
            appc.toString(),
            "APPC",
            targetCurrency,
            FIAT_SCALE
          ),
          localCurrencyConversionService.getValueToFiat(
            eth.toString(),
            "ETH",
            targetCurrency,
            FIAT_SCALE
          ),
          { appcCreditsFiat, appcFiat, ethFiat ->

            WalletBalance()
          }
        )
      }
  }

  private fun mapToTokenBalance(
    balance: Balance,
    fiatValue: FiatValue,
    currency: String
  ): TokenBalance {
    return TokenBalance(TokenValue(balance.value, currency, balance.symbol), fiatValue)
  }

  fun getStringValue(value: String): String {
    return value
      .stripTrailingZeros()
      .toPlainString()
  }

  interface WalletInfoApi {
    @GET("/transaction/wallet/{address}/info")
    fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
  }
}