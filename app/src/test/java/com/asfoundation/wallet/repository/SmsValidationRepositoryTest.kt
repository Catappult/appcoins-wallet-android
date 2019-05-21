package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.service.SMSValidationApi
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SMSValidationRepositoryTest {

    @Mock
    lateinit var smsValidationApi: SMSValidationApi

    private lateinit var smsValidationRepository: SMSValidationRepository
    private lateinit var walletAddress: String
    private lateinit var phoneNumber: String
    private lateinit var code: String

    @Before
    fun setUp() {
        walletAddress = "0x648234234"
        phoneNumber = "00351912475564"
        code = "0345671"

        smsValidationRepository = SMSValidationRepository(smsValidationApi)
    }

    @Test
    fun validateWallet() {
        val expectedWalletStatus = WalletStatus(walletAddress, true)

        `when`(smsValidationApi.validateWallet(walletAddress)).thenReturn(Single.just(expectedWalletStatus))

        val testObserver = smsValidationRepository.validateWallet(walletAddress).test()

        testObserver.assertNoErrors()
        testObserver.assertValue { walletStatus: WalletStatus ->
            walletStatus.walletAddress == walletAddress && walletStatus.verified
        }
    }

    @Test
    fun validateWallet_whenNotVerified_shouldThrowException() {
        val expectedWalletStatus = WalletStatus(walletAddress, false)

        `when`(smsValidationApi.validateWallet(walletAddress)).thenReturn(Single.just(expectedWalletStatus))

        val testObserver = smsValidationRepository.validateWallet(walletAddress).test()

        Assert.assertEquals(testObserver.errorCount(), 1)
    }

    @Test
    fun requestValidationCode() {
        val requestValidationCodeResponse = ValidationCodeResponse(phoneNumber)

        `when`(smsValidationApi.requestValidationCode(phoneNumber)).thenReturn(Single.just(requestValidationCodeResponse))

        val testObserver = smsValidationRepository.requestValidationCode(phoneNumber).test()

        testObserver.assertNoErrors()
        testObserver.assertValue { response: ValidationCodeResponse ->
            response.phone == phoneNumber
        }
    }

    @Test
    fun validateCode() {
        val expectedWalletStatus = WalletStatus(walletAddress, true)

        `when`(smsValidationApi.validateCode(phoneNumber, code, walletAddress)).thenReturn(Single.just(expectedWalletStatus))

        val testObserver = smsValidationApi.validateCode(phoneNumber, code, walletAddress).test()

        testObserver.assertNoErrors()
        testObserver.assertValue { walletStatus: WalletStatus ->
            walletStatus.walletAddress == walletAddress && walletStatus.verified
        }
    }

}