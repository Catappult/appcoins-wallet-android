package com.asfoundation.wallet.util

import android.net.Uri
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.commons.Repository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.TokenRepositoryType
import com.asfoundation.wallet.service.CurrencyConversionService
import io.reactivex.Single
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode


class OneStepTransactionParser(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                               private val tokenRepositoryType: TokenRepositoryType,
                               private val proxyService: ProxyService,
                               private val billing: Billing,
                               private val conversionService: CurrencyConversionService,
                               private val cache: Repository<String, TransactionBuilder>) {

  companion object {
    private const val SCHEME = "https"
    private const val HOST = BuildConfig.PAYMENT_HOST
    private const val PATH = "/transaction"
    private const val PAYMENT_TYPE_INAPP = "inapp"
    private const val PAYMENT_TYPE_INAPP_UNMANAGED = "inapp_unmanaged"
    private const val PAYMENT_TYPE_DONATION = "donation"
    private val PAYMENT_TYPES =
        arrayListOf(PAYMENT_TYPE_INAPP, PAYMENT_TYPE_INAPP_UNMANAGED, PAYMENT_TYPE_DONATION)
    private const val PARAM_VALUE = "value"
    private const val PARAM_TO = "to"
    private const val PARAM_PRODUCT = "product"
    private const val PARAM_DOMAIN = "domain"
    private const val PARAM_DATA = "data"
    private const val PARAM_CURRENCY = "currency"
    private const val PARAM_CALLBACK_URL = "callback_url"
    private const val NETWORK_ID_ROPSTEN = 3L
    private const val NETWORK_ID_MAIN = 1L


    fun isOneStepURLString(url: String): Boolean {
      val uri = Uri.parse(url)
      return uri.scheme == SCHEME && uri.host == HOST && uri.path.startsWith(
          PATH)
    }
  }

  fun buildTransaction(uriStr: String): Single<TransactionBuilder> {
    return if (cache.getSync(uriStr) != null) Single.just(cache.getSync(uriStr))
    else Single.zip(getToken(), getIabContract(), getWallet(Uri.parse(uriStr)),
        getTokenContract(),
        getAmount(Uri.parse(uriStr)),
        Function5 { token: Token, iabContract: String, walletAddress: String, tokenContract: String,
                    amount: BigDecimal ->
          val uri = Uri.parse(uriStr)
          TransactionBuilder(token.tokenInfo.symbol, tokenContract,
              getChainId(uri), walletAddress, amount, getSkuId(uri), 18, iabContract,
              getPaymentType(uri), getDomain(uri), getPayload(uri),
              getCallback(uri)).shouldSendToken(true)
        }).doOnSuccess { transactionBuilder ->
      cache.saveSync(uriStr, transactionBuilder)
    }.subscribeOn(Schedulers.io())
  }


  private fun getAmount(uri: Uri): Single<BigDecimal> {
    return if (uri.getQueryParameter(PARAM_VALUE) == null) {
      getProductValue(getDomain(uri), getSkuId(uri))
    } else {
      getTransactionValue(uri)
    }
  }

  private fun getToAddress(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_TO)
  }

  private fun getSkuId(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_PRODUCT)
  }

  private fun getDomain(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_DOMAIN)
  }

  private fun getPayload(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_DATA)
  }

  private fun getCurrency(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_CURRENCY)
  }

  private fun getChainId(uri: Uri): Long? {
    return if (uri.host == BuildConfig.PAYMENT_HOST_MAIN_NETWORK) NETWORK_ID_MAIN else NETWORK_ID_ROPSTEN
  }

  private fun getPaymentType(uri: Uri): String {
    return PAYMENT_TYPE_INAPP_UNMANAGED.toUpperCase()
  }

  private fun getToken(): Single<Token>? {
    return proxyService.getAppCoinsAddress(BuildConfig.DEBUG).flatMap { tokenAddress ->
      findDefaultWalletInteract.find().flatMap { wallet: Wallet ->
        tokenRepositoryType.fetchAll(wallet.address)
            .flatMapIterable { tokens: Array<Token> -> tokens.toCollection(ArrayList()) }
            .filter { token: Token -> token.tokenInfo.address.equals(tokenAddress, true) }
            .toList()
      }.map { tokens ->
        if (tokens.isEmpty()) {
          throw UnknownTokenException()
        } else {
          tokens[0]
        }
      }
    }
  }

  private fun getIabContract(): Single<String> {
    return proxyService.getIabAddress(BuildConfig.DEBUG)
  }

  private fun getTokenContract(): Single<String> {
    return proxyService.getAppCoinsAddress(BuildConfig.DEBUG)
  }

  private fun getWallet(uri: Uri): Single<String> {
    val domain = getDomain(uri)
    val toAddressWallet = getToAddress(uri)
    if (domain == null && toAddressWallet == null) {
      throw MissingWalletException()
    }

    return if (domain != null) billing.getWallet(domain) else Single.just(toAddressWallet)
  }

  private fun getProductValue(packageName: String?, skuId: String?): Single<BigDecimal> {
    return if (packageName != null && skuId != null) {
      billing.getProducts(packageName, listOf(skuId))
          .map { products -> products[0] }.map { product -> BigDecimal(product.price.amount) }
    } else {
      throw MissingProductException()
    }
  }

  private fun getTransactionValue(uri: Uri): Single<BigDecimal> {
    return if (getCurrency(uri) == null || getCurrency(uri) == "APPC") {
      Single.just(BigDecimal(uri.getQueryParameter(PARAM_VALUE)))
    } else {
      conversionService.getAppcRate(getCurrency(uri)).map {
        BigDecimal(uri.getQueryParameter(PARAM_VALUE)).divide(BigDecimal(it.amount), RoundingMode.HALF_UP)
      }
    }
  }

  private fun getCallback(uri: Uri): String? {
    return uri.getQueryParameter(PARAM_CALLBACK_URL)
  }
}

class MissingWalletException : RuntimeException()

class MissingProductException : RuntimeException()
