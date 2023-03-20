package com.asfoundation.wallet.util

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import com.appcoins.wallet.core.utils.jvm_common.Repository
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import io.reactivex.Single
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject


class OneStepTransactionParser @Inject constructor(
  private val proxyService: ProxyService,
  private val billing: Billing,
  private val defaultTokenProvider: DefaultTokenProvider
) {
  private val cache: Repository<String, TransactionBuilder> =
    MemoryCache(
      BehaviorSubject.create(), HashMap()
    )

  fun buildTransaction(oneStepUri: OneStepUri, referrerUrl: String): Single<TransactionBuilder> {
    return if (cache.getSync(oneStepUri.toString()) != null) {
      Single.just(cache.getSync(oneStepUri.toString()))
    } else {
      val completedOneStepUri = completeUri(oneStepUri)
      Single.zip(getToken(), getIabContract(), getWallet(oneStepUri), getTokenContract(),
        Function4 { token: Token, iabContract: String, walletAddress: String,
                    tokenContract: String ->
          val paymentType = if (isSkills(oneStepUri)) {
            Parameters.ESKILLS
          } else Parameters.PAYMENT_TYPE_INAPP_UNMANAGED
          TransactionBuilder(
            token.tokenInfo.symbol,
            tokenContract,
            getChainId(completedOneStepUri),
            walletAddress,
            getAppcAmount(completedOneStepUri),
            getSkuId(completedOneStepUri),
            token.tokenInfo.decimals,
            iabContract,
            paymentType,
            null,
            getDomain(completedOneStepUri),
            getPayload(completedOneStepUri),
            getCallback(completedOneStepUri),
            getOrderReference(completedOneStepUri),
            getProductToken(completedOneStepUri),
            getOriginAmount(completedOneStepUri),
            getOriginCurrency(completedOneStepUri),
            referrerUrl,
            ""
          ).shouldSendToken(true)
        })
        .doOnSuccess { transactionBuilder ->
          cache.saveSync(completedOneStepUri.toString(), transactionBuilder)
        }
        .subscribeOn(Schedulers.io())
    }
  }

  private fun getOrderReference(uri: OneStepUri): String? {
    return uri.parameters[Parameters.ORDER_REFERENCE]
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

  private fun getType(uri: OneStepUri): String {
    return uri.parameters[Parameters.TYPE] ?: "INAPP"
  }

  private fun getChainId(uri: OneStepUri): Long {
    return if (uri.host == HostProperties.BACKEND_HOST_NAME_DEV)
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

  private fun getCallback(uri: OneStepUri): String? {
    return uri.parameters[Parameters.CALLBACK_URL]
  }

  private fun getProductToken(uri: OneStepUri): String? {
    return uri.parameters[Parameters.PRODUCT_TOKEN]
  }

  private fun isSkills(uri: OneStepUri): Boolean {
    return uri.parameters[Parameters.SKILLS] != null
  }

  private fun completeUri(uri: OneStepUri): OneStepUri {
    val domain = getDomain(uri) ?: ""
    val sku = getSkuId(uri) ?: ""
    return if (uri.parameters.containsKey(Parameters.VALUE) && uri.parameters.containsKey(Parameters.CURRENCY)) {
      uri
    } else {   // osp url without price or currency, needs to include those params from product:
      val productDetails = getProductDetails(
        domain,
        sku
      )
      val paramsMap: MutableMap<String, String> = mutableMapOf()
      paramsMap.putAll(uri.parameters)
      paramsMap[Parameters.VALUE] = productDetails.transactionPrice.amount.toString()
      paramsMap[Parameters.CURRENCY] = productDetails.transactionPrice.currency

      OneStepUri(
        host = uri.host,
        parameters = paramsMap,
        path = uri.path,
        scheme = uri.scheme
      )
    }
  }

  private fun getProductDetails(domain: String, sku: String): Product {
    return billing.getProducts(domain, mutableListOf(sku), BillingSupportedType.INAPP)
      .subscribeOn(Schedulers.io())
      .map { products -> products.first() }
      .blockingGet()
  }

  class MissingWalletException : RuntimeException()

  class MissingAmountException : RuntimeException()
}