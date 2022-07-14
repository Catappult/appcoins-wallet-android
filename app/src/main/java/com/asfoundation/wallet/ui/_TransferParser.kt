package com.asfoundation.wallet.ui

import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.util.OneStepUri
import com.asfoundation.wallet.util.Parameters
import com.asfoundation.wallet.util.UnknownTokenException
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.util.encoders.Hex
import org.kethereum.erc681.ERC681
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class _EIPTransactionParser @Inject constructor(defaultTokenRepository: _DefaultTokenRepository) {

  private val tokenInfo = defaultTokenRepository.tokenInfo

  fun buildTransaction(erc681: ERC681): TransactionBuilder =
    when (erc681.function?.lowercase()) {
      "buy" -> buildAppcTransaction(erc681.checkToken())
      "transfer" -> buildTokenTransaction(erc681.checkToken())
      else -> buildEthTransaction(erc681)
    }

  private fun buildEthTransaction(payment: ERC681): TransactionBuilder =
    TransactionBuilder("ETH")
      .toAddress(payment.address)
      .amount(payment.etherTransferAmount)
      .apply { type = "inapp" } //Transfer only so it doesn't matter

  private fun buildTokenTransaction(payment: ERC681): TransactionBuilder =
    TransactionBuilder(
      tokenInfo.symbol,
      tokenInfo.address,
      payment.chainId,
      payment.receiverAddress,
      payment.getTokenTransferAmount(tokenInfo.decimals),
      tokenInfo.decimals
    ).shouldSendToken(true)

  private fun buildAppcTransaction(payment: ERC681): TransactionBuilder =
    payment.functionParams["data"]!!
      .substring(2)
      .toByteArray(StandardCharsets.UTF_8)
      .let { Hex.decode(it) }
      .toString(Charsets.UTF_8)
      .let { Gson().fromJson(it, TransactionData::class.java) }
      .let {
        TransactionBuilder(
          tokenInfo.symbol,
          payment.address,
          payment.chainId,
          payment.receiverAddress,
          payment.getTokenTransferAmount(tokenInfo.decimals),
          it.skuId,
          tokenInfo.decimals,
          payment.functionParams["iabContractAddress"],
          it.type,
          it.origin,
          it.domain,
          it.payload,
          null,
          it.orderReference,
          null,
          null,
          it.period,
          it.trialPeriod
        ).shouldSendToken(true)
      }

  private val ERC681.etherTransferAmount: BigDecimal
    get() = value?.toBigDecimal()
      ?.divide(BigDecimal(BigInteger.ONE, -18), 18, RoundingMode.DOWN)
      ?: BigDecimal.ZERO

  private fun ERC681.getTokenTransferAmount(decimals: Int): BigDecimal =
    functionParams["uint256"]?.toBigDecimal()
      ?.divide(BigDecimal(BigInteger.ONE, -decimals), decimals, RoundingMode.DOWN)
      ?: BigDecimal.ZERO

  private val ERC681.receiverAddress: String?
    get() = when (function?.lowercase()) {
      "transfer", "buy" -> functionParams["address"]
      else -> address
    }

  private fun ERC681.checkToken() = apply {
    if (!tokenInfo.address.equals(address, ignoreCase = true)) {
      throw UnknownTokenException()
    }
  }
}

class _OneStepTransactionParser @Inject constructor(
  private val proxySdk: AppCoinsAddressProxySdk,
  private val bdsApiSecondary: BdsApiSecondary,
  defaultTokenRepository: _DefaultTokenRepository
) {

  private val tokenInfo = defaultTokenRepository.tokenInfo
  private val cache: MutableMap<String, TransactionBuilder> = mutableMapOf()

  fun buildTransaction(uri: OneStepUri, referrerUrl: String): Single<TransactionBuilder> =
    Single.fromCallable { OneStepData(uri) }
      .subscribeOn(Schedulers.io())
      .flatMap { oneStepData ->
        Single.fromCallable { cache[uri.toString()] ?: throw NullPointerException() }
          .onErrorResumeNext {
            val networkId = if (BuildConfig.DEBUG) NETWORK_ID_ROPSTEN else NETWORK_ID_MAIN
            Single.zip(
              oneStepData.getWallet(),
              proxySdk.getIabAddress(networkId).subscribeOn(Schedulers.io()),
              proxySdk.getAppCoinsAddress(networkId).subscribeOn(Schedulers.io())
            ) { walletAddress, iabContract, tokenContract ->
              TransactionBuilder(
                tokenInfo.symbol,
                tokenContract,
                oneStepData.chainId,
                walletAddress,
                oneStepData.appcAmount,
                oneStepData.product,
                tokenInfo.decimals,
                iabContract,
                oneStepData.paymentType,
                null,
                oneStepData.domain,
                oneStepData.payload,
                oneStepData.callbackUrl,
                oneStepData.orderReference,
                oneStepData.productToken,
                oneStepData.amount,
                oneStepData.currency,
                referrerUrl,
                ""
              ).shouldSendToken(true)
            }
              .doOnSuccess { cache[uri.toString()] = it }
          }
      }

  private fun OneStepData.getWallet(): Single<String> =
    Single.fromCallable { domain ?: throw NullPointerException() }
      .flatMap { bdsApiSecondary.getWallet(it).subscribeOn(Schedulers.io()) }
      .map { it.data.address }
      .onErrorReturn { toAddressWallet ?: throw _MissingWalletException() }

  private class OneStepData(oneStepUri: OneStepUri) {
    val chainId = if (oneStepUri.host == BuildConfig.PAYMENT_HOST_ROPSTEN_NETWORK) {
      Parameters.NETWORK_ID_ROPSTEN
    } else {
      Parameters.NETWORK_ID_MAIN
    }
    val domain = oneStepUri.parameters[Parameters.DOMAIN]
    val toAddressWallet = oneStepUri.parameters[Parameters.TO]
    val paymentType = if (oneStepUri.parameters.containsKey(Parameters.SKILLS)) {
      Parameters.ESKILLS
    } else {
      Parameters.PAYMENT_TYPE_INAPP_UNMANAGED
    }
    val currency = oneStepUri.parameters[Parameters.CURRENCY] ?: "APPC"
    val amount = oneStepUri.parameters[Parameters.VALUE] ?: throw _MissingAmountException()
    val product = oneStepUri.parameters[Parameters.PRODUCT]
    val payload = oneStepUri.parameters[Parameters.DATA]
    val callbackUrl = oneStepUri.parameters[Parameters.CALLBACK_URL]
    val orderReference = oneStepUri.parameters[Parameters.ORDER_REFERENCE]
    val productToken = oneStepUri.parameters[Parameters.PRODUCT_TOKEN]

    // To avoid spending time, during the parse, getting the appc value by calling the conversion of
    // fiat to appc, we are setting the amount to zero. Later when this value is zero we should make
    // the request to get the conversion and set it on the transaction builder.
    val appcAmount: BigDecimal? =
      if (currency.uppercase() == "APPC") BigDecimal(amount).setScale(18)
      else BigDecimal.ZERO

    init {
      if (domain == null && toAddressWallet == null) throw _MissingWalletException()
    }
  }

  companion object {
    private const val NETWORK_ID_ROPSTEN = 3
    private const val NETWORK_ID_MAIN = 1
  }
}
