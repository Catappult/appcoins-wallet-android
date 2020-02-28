/*
package com.asfoundation.wallet.repository

import com.asfoundation.wallet.service.WalletBalance
import com.asfoundation.wallet.service.WalletBalanceService
import com.google.gson.Gson
import io.reactivex.Single
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class WalletBalanceRepositoryTest {

  @Mock
  lateinit var walletBalanceService: WalletBalanceService
  @Mock
  lateinit var gson: Gson
  private lateinit var walletBalanceRepository: WalletBalanceRepository
  private lateinit var walletAddress: String
  private lateinit var appcBalance: BigInteger
  private lateinit var ethBalance: BigInteger

  @Before
  fun setUp() {
    walletAddress = "0x648234234"
    appcBalance = BigInteger("18958500000004718592")
    ethBalance = BigInteger("840819205381085000")
    walletBalanceRepository = WalletBalanceRepository(walletBalanceService)
  }


  @Test
  fun getEthBalance_whenOk_shouldReturnBalance() {
    val walletBalance = WalletBalance(appcBalance, ethBalance)

    `when`(walletBalanceService.getWalletBalance(walletAddress)).thenReturn(
        Single.just(walletBalance))

    val testObserver = walletBalanceRepository.getEthBalance(walletAddress)
        .test()

    val expectedResult = BigDecimal(ethBalance)

    testObserver.assertNoErrors()
    testObserver.assertValue(expectedResult)
  }

  @Test
  fun getAppcBalance_whenOk_shouldReturnBalance() {
    val walletBalance = WalletBalance(appcBalance, ethBalance)

    `when`(walletBalanceService.getWalletBalance(walletAddress)).thenReturn(
        Single.just(walletBalance))

    val testObserver = walletBalanceRepository.getAppcBalance(walletAddress)
        .test()

    val expectedResult = BigDecimal(appcBalance)

    testObserver.assertNoErrors()
    testObserver.assertValue(expectedResult)
  }

}*/
