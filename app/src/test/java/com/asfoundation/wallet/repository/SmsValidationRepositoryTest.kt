package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletRequestCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.service.SmsValidationApi
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SmsValidationRepositoryTest {

  @Mock
  lateinit var smsValidationApi: SmsValidationApi

  private lateinit var smsValidationRepository: SmsValidationRepository
  private lateinit var walletAddress: String
  private lateinit var phoneNumber: String
  private lateinit var code: String

  @Before
  fun setUp() {
    walletAddress = "0x648234234"
    phoneNumber = "00351912475564"
    code = "0345671"

    smsValidationRepository = SmsValidationRepository(smsValidationApi)
  }

  @Test
  fun validateWallet() {
    val expectedWalletStatus = WalletStatus(walletAddress, true)

    `when`(smsValidationApi.isValid(walletAddress)).thenReturn(Single.just(expectedWalletStatus))

    val testObserver = smsValidationRepository.isValid(walletAddress)
        .test()

    testObserver.assertNoErrors()
    testObserver.assertValue(WalletValidationStatus.SUCCESS)
  }

  @Test
  fun validateWallet_whenNotVerified_shouldReturnUnverified() {
    val expectedWalletStatus = WalletStatus(walletAddress, false)

    `when`(smsValidationApi.isValid(walletAddress)).thenReturn(Single.just(expectedWalletStatus))

    val testObserver = smsValidationRepository.isValid(walletAddress)
        .test()

    testObserver.assertNoErrors()
    testObserver.assertValue(WalletValidationStatus.GENERIC_ERROR)
  }

  @Test
  fun requestValidationCode() {
    val requestValidationCodeResponse = WalletRequestCodeResponse(phoneNumber)

    `when`(smsValidationApi.requestValidationCode(phoneNumber)).thenReturn(
        Single.just(requestValidationCodeResponse))

    val testObserver = smsValidationRepository.requestValidationCode(phoneNumber)
        .test()

    testObserver.assertNoErrors()
    testObserver.assertValue(WalletValidationStatus.SUCCESS)
  }

  @Test
  fun validateCode() {
    val expectedWalletStatus = WalletStatus(walletAddress, true)

    `when`(smsValidationApi.validateCode(phoneNumber, walletAddress, code)).thenReturn(
        Single.just(expectedWalletStatus))

    val testObserver = smsValidationRepository.validateCode(phoneNumber, walletAddress, code)
        .test()

    testObserver.assertNoErrors()
    testObserver.assertValue(WalletValidationStatus.SUCCESS)
  }

}