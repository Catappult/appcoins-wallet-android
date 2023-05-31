package com.asfoundation.wallet.util

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.service.TokenRateService
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.*

class OneStepWatchedTransactionParserTest {
  private lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  private lateinit var defaultTokenProvider: DefaultTokenProvider
  private lateinit var proxyService: ProxyService
  private lateinit var billing: Billing
  private lateinit var conversionService: TokenRateService

  companion object {
    const val contractAddress = "contract_address"
    const val iabContractAddress = "iab_contract_address"
    const val developerAddress = "developer_address"
    const val wrongDeveloperAddress = "wrong_developer_address"
    const val productName = "product_name"
    const val packageName = "package_name"
    const val paymentType = "INAPP_UNMANAGED"
    const val developerPayload = "developer_payload"
    const val priceValue = "2.3"
    const val currency = "APPC"
    const val callback = "callback_url"
    const val url = "a_random_uri"
  }

  @Before
  fun before() {
    findDefaultWalletInteract =
        mock<FindDefaultWalletInteract>(FindDefaultWalletInteract::class.java)
    proxyService = mock<ProxyService>(ProxyService::class.java)
    billing = mock<Billing>(Billing::class.java)
    conversionService = mock<TokenRateService>(TokenRateService::class.java)
    defaultTokenProvider = mock<DefaultTokenProvider>(DefaultTokenProvider::class.java)

    `when`<Single<Wallet>>(findDefaultWalletInteract.find()).thenReturn(
        Single.just(Wallet(contractAddress)))
    val tokenInfo = TokenInfo(contractAddress, "AppCoins", "APPC", 18)

    `when`(defaultTokenProvider.defaultToken)
        .thenReturn(Single.just(tokenInfo))
    `when`<Single<String>>(proxyService.getAppCoinsAddress(anyBoolean())).thenReturn(
        Single.just(contractAddress))
    `when`<Single<String>>(proxyService.getIabAddress(anyBoolean())).thenReturn(
        Single.just(iabContractAddress))

    `when`<Single<FiatValue>>(conversionService.getAppcRate(anyString())).thenReturn(
        Single.just(FiatValue(BigDecimal("0.07"), "EUR")))

    `when`<Single<String>>(billing.getWallet(anyString())).thenReturn(
        Single.just(developerAddress))
  }

  @Test
  @Throws(InterruptedException::class)
  fun parseTransaction() {

    val oneStepTransactionParser =
        OneStepTransactionParser(proxyService, billing, defaultTokenProvider)

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["currency"] = currency
    parameters["domain"] = packageName
    parameters["data"] = developerPayload
    parameters["callback_url"] = callback

    val oneStepUri =
        OneStepUri("https", "apichain.dev.catappult.io", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(oneStepUri, url)
        .test()
        .await()

    println(test.values())

    test.assertValue { transactionBuilder ->
      transactionBuilder.amount() == BigDecimal(
          priceValue).setScale(18)
    }
    test.assertValue { transactionBuilder ->
      transactionBuilder.toAddress()
          .equals(developerAddress, ignoreCase = true)
    }
    test.assertValue { transactionBuilder -> transactionBuilder.contractAddress() == contractAddress }
    test.assertValue { transactionBuilder -> transactionBuilder.chainId == 3L }
    test.assertValue { transactionBuilder -> transactionBuilder.type == paymentType }
    test.assertValue { transactionBuilder -> transactionBuilder.domain == packageName }
    test.assertValue { transactionBuilder -> transactionBuilder.iabContract == iabContractAddress }
    test.assertValue { transactionBuilder -> transactionBuilder.skuId == null }
    test.assertValue { transactionBuilder -> transactionBuilder.payload == developerPayload }
    test.assertValue { transactionBuilder -> transactionBuilder.callbackUrl == callback }
  }

  @Test
  @Throws(InterruptedException::class)
  fun parseMinimumTransaction() {

    val oneStepTransactionParser =
        OneStepTransactionParser(proxyService, billing, defaultTokenProvider)

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["currency"] = currency
    parameters["domain"] = packageName

    val oneStepUri =
        OneStepUri("https", "apichain.dev.catappult.io", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(oneStepUri, url)
        .test()
        .await()

    println(test.values())

    test.assertValue { transactionBuilder ->
      transactionBuilder.amount() == BigDecimal(
          priceValue).setScale(18)
    }
    test.assertValue { transactionBuilder ->
      transactionBuilder.toAddress()
          .equals(developerAddress, ignoreCase = true)
    }
    test.assertValue { transactionBuilder -> transactionBuilder.contractAddress() == contractAddress }
    test.assertValue { transactionBuilder -> transactionBuilder.chainId == 3L }
  }

  @Test
  @Throws(InterruptedException::class)
  fun parseTransactionWithFiatValue() {

    val oneStepTransactionParser =
        OneStepTransactionParser(proxyService, billing, defaultTokenProvider)

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["currency"] = "EUR"
    parameters["domain"] = packageName
    parameters["to"] = wrongDeveloperAddress

    val oneStepUri =
        OneStepUri("https", "apichain.dev.catappult.io", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(oneStepUri, url)
        .test()
        .await()

    test.assertValue { transactionBuilder ->
      transactionBuilder.amount() == BigDecimal.ZERO
    }
    test.assertValue { transactionBuilder ->
      transactionBuilder.toAddress()
          .equals(developerAddress, ignoreCase = true)
    }
    test.assertValue { transactionBuilder -> transactionBuilder.contractAddress() == contractAddress }
    test.assertValue { transactionBuilder -> transactionBuilder.chainId == 3L }
  }
}