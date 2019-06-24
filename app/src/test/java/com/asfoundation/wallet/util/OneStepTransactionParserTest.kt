package com.asfoundation.wallet.util

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.commons.MemoryCache
import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.TokenRepositoryType
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.*

class OneStepTransactionParserTest {
  private lateinit var tokenRepositoryType: TokenRepositoryType
  private lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  private lateinit var proxyService: ProxyService
  private lateinit var billing: Billing
  private lateinit var conversionService: TokenRateService

  companion object {
    val contractAddress = "contract_address"
    val iabContractAddress = "iab_contract_address"
    val developerAddress = "developer_address"
    val wrongDeveloperAddress = "wrong_developer_address"
    val productName = "product_name"
    val packageName = "package_name"
    val paymentType = "INAPP_UNMANAGED"
    val developerPayload = "developer_payload"
    val priceValue = "2.3"
    val currency = "APPC"
    val callback = "callback_url"
  }

  @Before
  fun before() {
    tokenRepositoryType = mock<TokenRepositoryType>(TokenRepositoryType::class.java)
    findDefaultWalletInteract =
        mock<FindDefaultWalletInteract>(FindDefaultWalletInteract::class.java)
    proxyService = mock<ProxyService>(ProxyService::class.java)
    billing = mock<Billing>(Billing::class.java)
    conversionService = mock<TokenRateService>(TokenRateService::class.java)

    `when`<Single<Wallet>>(findDefaultWalletInteract.find()).thenReturn(
        Single.just(Wallet(contractAddress)))
    val token = Token(TokenInfo(contractAddress, "AppCoins", "APPC", 18, true, true),
        BigDecimal(10), 32L)
    val tokens = arrayOf(token)

    `when`<Observable<Array<Token>>>(tokenRepositoryType.fetchAll(any<String>())).thenReturn(
        Observable.just<Array<Token>>(tokens))

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
        OneStepTransactionParser(findDefaultWalletInteract, tokenRepositoryType, proxyService,
            billing, conversionService,
            MemoryCache(BehaviorSubject.create(), HashMap()))

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["currency"] = currency
    parameters["domain"] = packageName
    parameters["product"] = productName
    parameters["data"] = developerPayload
    parameters["callback_url"] = callback

    val uri = OneStepUri("https", "apichain-dev.blockchainds.com", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(uri)
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
    test.assertValue { transactionBuilder -> transactionBuilder.skuId == productName }
    test.assertValue { transactionBuilder -> transactionBuilder.payload == developerPayload }
    test.assertValue { transactionBuilder -> transactionBuilder.callbackUrl == callback }
  }

  @Test
  @Throws(InterruptedException::class)
  fun parseMinimumTransaction() {

    val oneStepTransactionParser =
        OneStepTransactionParser(findDefaultWalletInteract, tokenRepositoryType, proxyService,
            billing, conversionService,
            MemoryCache(BehaviorSubject.create(), HashMap()))

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["domain"] = packageName

    val uri = OneStepUri("https", "apichain-dev.blockchainds.com", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(uri)
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
  fun parseTransactionWithConversion() {

    val oneStepTransactionParser =
        OneStepTransactionParser(findDefaultWalletInteract, tokenRepositoryType, proxyService,
            billing, conversionService,
            MemoryCache(BehaviorSubject.create(), HashMap()))

    val parameters = HashMap<String, String>()
    parameters["value"] = priceValue
    parameters["currency"] = "EUR"
    parameters["domain"] = packageName
    parameters["to"] = wrongDeveloperAddress

    val uri = OneStepUri("https", "apichain-dev.blockchainds.com", "/transaction/inapp", parameters)
    val test = oneStepTransactionParser.buildTransaction(uri)
        .test()
        .await()

    test.assertValue { transactionBuilder ->
      transactionBuilder.amount() == BigDecimal(
          "32.857142857142857143")
    }
    test.assertValue { transactionBuilder ->
      transactionBuilder.toAddress()
          .equals(developerAddress, ignoreCase = true)
    }
    test.assertValue { transactionBuilder -> transactionBuilder.contractAddress() == contractAddress }
    test.assertValue { transactionBuilder -> transactionBuilder.chainId == 3L }
  }
}