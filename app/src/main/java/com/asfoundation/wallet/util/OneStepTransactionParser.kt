package com.asfoundation.wallet.util

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.commons.Repository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.service.TokenRateService
import io.reactivex.Single
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode


class OneStepTransactionParser(
    private val proxyService: ProxyService,
    private val billing: Billing,
    private val conversionService: TokenRateService,
    private val cache: Repository<String, TransactionBuilder>,
    private val defaultTokenProvider: DefaultTokenProvider) {

  fun buildTransaction(oneStepUri: OneStepUri, referrerUrl: String): Single<TransactionBuilder> {
    return if (cache.getSync(oneStepUri.toString()) != null) {
      Single.just(cache.getSync(oneStepUri.toString()))
    } else {
      getSkuDetails(oneStepUri)
          .flatMap { skuDetailsResponse: SkuDetailsResponse ->
            Single.zip(getToken(), getIabContract(), getWallet(oneStepUri), getTokenContract(),
                getAmount(oneStepUri, skuDetailsResponse.product),
                Function5 { token: Token, iabContract: String, walletAddress: String,
                            tokenContract: String, amount: BigDecimal ->
                  val type = mapPaymentType(oneStepUri)
                  TransactionBuilder(token.tokenInfo.symbol, tokenContract, getChainId(oneStepUri),
                      walletAddress, amount, getSkuId(oneStepUri), token.tokenInfo.decimals,
                      iabContract, type, null,
                      getDomain(oneStepUri), getPayload(oneStepUri), getCallback(oneStepUri),
                      getOrderReference(oneStepUri), mapReferrer(referrerUrl, type),
                      skuDetailsResponse.product?.title.orEmpty()).shouldSendToken(true)
                })
                .map {
                  it.originalOneStepValue = oneStepUri.parameters[Parameters.VALUE]
                  var currency = oneStepUri.parameters[Parameters.CURRENCY]
                  if (currency == null) {
                    currency = "APPC"
                  }
                  it.originalOneStepCurrency = currency
                  it
                }
                .doOnSuccess { transactionBuilder ->
                  cache.saveSync(oneStepUri.toString(), transactionBuilder)
                }
          }
          .subscribeOn(Schedulers.io())
    }
  }

  private fun mapReferrer(referrerUrl: String, type: String): String? {
    return when (type) {
      Parameters.PAYMENT_TYPE_VOUCHER -> null
      else -> referrerUrl
    }
  }

  private fun mapPaymentType(oneStepUri: OneStepUri): String {
    val split = oneStepUri.path.split("transaction/")
    if (split.size >= 2 && split[1].isNotEmpty()) {
      split[1].let { type ->
        return when (type) {
          "voucher" -> Parameters.PAYMENT_TYPE_VOUCHER
          "donation" -> Parameters.PAYMENT_TYPE_DONATION
          else -> Parameters.PAYMENT_TYPE_INAPP_UNMANAGED
        }
      }
    }
    return Parameters.PAYMENT_TYPE_INAPP_UNMANAGED
  }

  private fun getOrderReference(uri: OneStepUri): String? {
    return uri.parameters["order_reference"]
  }

  private fun getAmount(uri: OneStepUri, product: Product?): Single<BigDecimal> {
    return when {
      product != null -> Single.just(BigDecimal(product.price.appcoinsAmount))
      uri.parameters[Parameters.VALUE] != null -> getTransactionValue(uri)
      else -> Single.error(MissingAmountException())
    }
  }

  private fun getToAddress(uri: OneStepUri): String? {
    return uri.parameters[Parameters.TO]
  }

  private fun getSkuId(uri: OneStepUri): String? {
    return uri.parameters[Parameters.PRODUCT]
  }

  private fun getDomain(uri: OneStepUri): String? {
    return uri.parameters[Parameters.DOMAIN]
  }

  private fun getPayload(uri: OneStepUri): String? {
    return uri.parameters[Parameters.DATA]
  }

  private fun getCurrency(uri: OneStepUri): String? {
    return uri.parameters[Parameters.CURRENCY]
  }

  private fun getChainId(uri: OneStepUri): Long {
    return if (uri.host == BuildConfig.PAYMENT_HOST_ROPSTEN_NETWORK)
      Parameters.NETWORK_ID_ROPSTEN else Parameters.NETWORK_ID_MAIN
  }

  private fun getToken(): Single<Token> {
    return defaultTokenProvider.defaultToken
        .map { Token(it, BigDecimal.ZERO) }
  }

  private fun getIabContract(): Single<String> {
    return proxyService.getIabAddress(BuildConfig.DEBUG)
  }

  private fun getTokenContract(): Single<String> {
    return proxyService.getAppCoinsAddress(BuildConfig.DEBUG)
  }

  private fun getWallet(uri: OneStepUri): Single<String> {
    val domain = getDomain(uri)
    val toAddressWallet = getToAddress(uri)
    if (domain == null && toAddressWallet == null) {
      return Single.error(MissingWalletException())
    }

    return if (domain != null) {
      billing.getWallet(domain)
          .onErrorReturn {
            toAddressWallet
          }
    } else {
      Single.just(toAddressWallet)
    }
  }

  private fun getSkuDetails(uri: OneStepUri): Single<SkuDetailsResponse> {
    val domain = getDomain(uri)
    val skuId = getSkuId(uri)
    return if (domain != null && skuId != null) {
      billing.getProducts(domain, listOf(skuId))
          .map { products -> SkuDetailsResponse(products[0]) }
          .onErrorReturn { SkuDetailsResponse(null) }
    } else Single.just(SkuDetailsResponse(null))
  }

  private fun getTransactionValue(uri: OneStepUri): Single<BigDecimal> {
    return if (getCurrency(uri) == null || getCurrency(uri).equals("APPC", true)) {
      Single.just(BigDecimal(uri.parameters[Parameters.VALUE]).setScale(18))
    } else {
      conversionService.getAppcRate(getCurrency(uri)!!.toUpperCase())
          .map {
            BigDecimal(uri.parameters[Parameters.VALUE])
                .divide(it.amount, 18, RoundingMode.UP)
          }
    }
  }

  private fun getCallback(uri: OneStepUri): String? {
    return uri.parameters[Parameters.CALLBACK_URL]
  }
}

class MissingWalletException : RuntimeException()

class MissingAmountException : RuntimeException()

class SkuDetailsResponse(val product: Product?)