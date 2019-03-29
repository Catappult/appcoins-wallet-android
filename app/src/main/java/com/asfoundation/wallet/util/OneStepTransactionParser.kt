package com.asfoundation.wallet.util

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.commons.Repository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.TokenRepositoryType
import com.asfoundation.wallet.service.TokenRateService
import io.reactivex.Single
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode


class OneStepTransactionParser(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                               private val tokenRepositoryType: TokenRepositoryType,
                               private val proxyService: ProxyService,
                               private val billing: Billing,
                               private val conversionService: TokenRateService,
                               private val cache: Repository<String, TransactionBuilder>) {

  fun buildTransaction(uri: OneStepUri): Single<TransactionBuilder> {
    return if (cache.getSync(uri.toString()) != null) Single.just(cache.getSync(uri.toString()))
    else Single.zip(getToken(), getIabContract(), getWallet(uri),
        getTokenContract(),
        getAmount(uri),
        Function5 { token: Token, iabContract: String, walletAddress: String, tokenContract: String,
                    amount: BigDecimal ->
          TransactionBuilder(token.tokenInfo.symbol, tokenContract,
              getChainId(uri), walletAddress, amount, getSkuId(uri), token.tokenInfo.decimals,
              iabContract, Parameters.PAYMENT_TYPE_INAPP_UNMANAGED.toUpperCase(), null,
              getDomain(uri), getPayload(uri), getCallback(uri),
              getOrderReference(uri)).shouldSendToken(true)
        }).map {
      it.originalOneStepValue = uri.parameters[Parameters.VALUE]
      var currency = uri.parameters[Parameters.CURRENCY]
      if (currency == null) {
        currency = "APPC"
      }
      it.originalOneStepCurrency = currency
      return@map it
    }.doOnSuccess { transactionBuilder ->
      cache.saveSync(uri.toString(), transactionBuilder)
    }.subscribeOn(Schedulers.io())
  }

  private fun getOrderReference(uri: OneStepUri): String? {
    return uri.parameters["order_reference"]
  }

  private fun getAmount(uri: OneStepUri): Single<BigDecimal> {
    return if (uri.parameters[Parameters.VALUE] == null) {
      getProductValue(getDomain(uri), getSkuId(uri))
    } else {
      getTransactionValue(uri)
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
    return proxyService.getAppCoinsAddress(BuildConfig.DEBUG).flatMap { tokenAddress ->
      findDefaultWalletInteract.find().flatMap { wallet: Wallet ->
        tokenRepositoryType.fetchAll(wallet.address)
            .flatMapIterable { tokens: Array<Token> -> tokens.toCollection(ArrayList()) }
            .filter { token: Token -> token.tokenInfo.address.equals(tokenAddress, true) }
            .toList()
      }.flatMap { tokens ->
        if (tokens.isEmpty()) {
          Single.error(UnknownTokenException())
        } else {
          Single.just(tokens[0])
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

  private fun getWallet(uri: OneStepUri): Single<String> {
    val domain = getDomain(uri)
    val toAddressWallet = getToAddress(uri)
    if (domain == null && toAddressWallet == null) {
      return Single.error(MissingWalletException())
    }

    return if (domain != null) {
      billing.getWallet(domain).onErrorReturn {
        toAddressWallet
      }
    } else {
      Single.just(toAddressWallet)
    }
  }

  private fun getProductValue(packageName: String?, skuId: String?): Single<BigDecimal> {
    return if (packageName != null && skuId != null) {
      billing.getProducts(packageName, listOf(skuId))
          .map { products -> products[0] }
          .map { product -> BigDecimal(product.price.appcoinsAmount) }
    } else {
      Single.error(MissingProductException())
    }
  }

  private fun getTransactionValue(uri: OneStepUri): Single<BigDecimal> {
    return if (getCurrency(uri) == null || getCurrency(uri).equals("APPC", true)) {
      Single.just(BigDecimal(uri.parameters[Parameters.VALUE]).setScale(18))
    } else {
      conversionService.getAppcRate(getCurrency(uri)!!.toUpperCase()).map {
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

class MissingProductException : RuntimeException()
