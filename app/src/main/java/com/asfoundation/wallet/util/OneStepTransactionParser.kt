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
import io.reactivex.functions.Function4
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode


class OneStepTransactionParser(
  private val proxyService: ProxyService,
  private val billing: Billing,
  private val conversionService: TokenRateService,
  private val cache: Repository<String, TransactionBuilder>,
  private val defaultTokenProvider: DefaultTokenProvider
) {

  fun buildTransaction(oneStepUri: OneStepUri, referrerUrl: String): Single<TransactionBuilder> {
    return if (cache.getSync(oneStepUri.toString()) != null) {
      Single.just(cache.getSync(oneStepUri.toString()))
    } else {
      Single.zip(getToken(), getIabContract(), getWallet(oneStepUri), getTokenContract(),
        Function4 { token: Token, iabContract: String, walletAddress: String,
                    tokenContract: String ->
          val paymentType = if (isSkills(oneStepUri)) {
            Parameters.ESKILLS
          } else Parameters.PAYMENT_TYPE_INAPP_UNMANAGED
          TransactionBuilder(
            token.tokenInfo.symbol,
            tokenContract,
            getChainId(oneStepUri),
            walletAddress,
            getAppcAmount(oneStepUri),
            getSkuId(oneStepUri),
            token.tokenInfo.decimals,
            iabContract,
            paymentType,
            null,
            getDomain(oneStepUri),
            getPayload(oneStepUri),
            getCallback(oneStepUri),
            getOrderReference(oneStepUri),
            getProductToken(oneStepUri),
            getOriginAmount(oneStepUri),
            getOriginCurrency(oneStepUri),
            referrerUrl,
            ""
          ).shouldSendToken(true)
        })
        .doOnSuccess { transactionBuilder ->
          cache.saveSync(oneStepUri.toString(), transactionBuilder)
        }
        .subscribeOn(Schedulers.io())
    }
  }

  private fun getOrderReference(uri: OneStepUri): String? {
    return uri.parameters["order_reference"]
  }

  private fun getOriginAmount(uri: OneStepUri): String {
    return when {
      uri.parameters[Parameters.VALUE] != null -> uri.parameters[Parameters.VALUE]!!
      else -> throw MissingAmountException()
    }
  }

  private fun getOriginCurrency(uri: OneStepUri): String? {
    var currency = uri.parameters[Parameters.CURRENCY]
    return when (currency) {
      null -> "APPC"
      else -> currency
    }
  }

  private fun getAppcAmount(uri: OneStepUri): BigDecimal? {
    // To avoid spending time, during the parse, getting the appc value by calling the conversion of
    // fiat to appc, we are setting the amount to zero. Later when this value is zero we should make
    // the request to get the conversion and set it on the transaction builder.
    return when {
      (getCurrency(uri) == null || getCurrency(uri).equals("APPC", true)) ->
        BigDecimal(uri.parameters[Parameters.VALUE]).setScale(18)
      else -> BigDecimal.ZERO
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

  private fun getProductToken(uri: OneStepUri): String? {
    return uri.parameters[Parameters.PRODUCT_TOKEN]
  }

  private fun isSkills(uri: OneStepUri): Boolean {
    return uri.parameters[Parameters.SKILLS] != null
  }
}

class MissingWalletException : RuntimeException()

class MissingAmountException : RuntimeException()

class SkuDetailsResponse(val product: Product?)