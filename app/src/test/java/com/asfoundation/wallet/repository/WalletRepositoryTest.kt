package com.asfoundation.wallet.repository

import com.asfoundation.wallet.analytics.AmplitudeAnalytics
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.WalletBalance
import com.asfoundation.wallet.service.WalletBalanceService
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class WalletRepositoryTest {

  @Mock
  lateinit var preferencesRepositoryType: PreferencesRepositoryType

  @Mock
  lateinit var accountKeystoreService: AccountKeystoreService

  @Mock
  lateinit var walletBalanceService: WalletBalanceService

  @Mock
  lateinit var analyticsSetup: AnalyticsSetup

  @Mock
  lateinit var amplitudeAnalytics: AmplitudeAnalytics

  private lateinit var walletAddress: String
  private lateinit var wallet: Wallet
  private lateinit var walletRepository: WalletRepository
  private lateinit var testScheduler: TestScheduler

  @Before
  fun setUp() {
    walletAddress = "0x648234234"
    wallet = Wallet(walletAddress)
    testScheduler = TestScheduler()
    walletRepository =
        WalletRepository(preferencesRepositoryType, accountKeystoreService, walletBalanceService,
            testScheduler, analyticsSetup, amplitudeAnalytics)
  }

  @Test
  fun getEthBalanceInWei() {
    val appcBalance = BigInteger("18958500000004718592")
    val ethBalance = BigInteger("840819205381085000")
    val walletBalance = WalletBalance(appcBalance, ethBalance)
    `when`(walletBalanceService.getWalletBalance(anyString()))
        .thenReturn(Single.just(walletBalance))

    val observer = TestObserver<BigDecimal>()

    walletRepository.getEthBalanceInWei(wallet.address)
        .subscribe(observer)

    testScheduler.triggerActions()

    observer.assertNoErrors()
        .assertValue { it == BigDecimal(ethBalance) }

  }

  @Test
  fun getAppcBalanceInWei() {
    val appcBalance = BigInteger("18958500000004718592")
    val ethBalance = BigInteger("840819205381085000")
    val walletBalance = WalletBalance(appcBalance, ethBalance)
    `when`(walletBalanceService.getWalletBalance(anyString()))
        .thenReturn(Single.just(walletBalance))

    val observer = TestObserver<BigDecimal>()

    walletRepository.getAppcBalanceInWei(wallet.address)
        .subscribe(observer)

    testScheduler.triggerActions()

    observer.assertNoErrors()
        .assertValue { it == BigDecimal(appcBalance) }

  }

}