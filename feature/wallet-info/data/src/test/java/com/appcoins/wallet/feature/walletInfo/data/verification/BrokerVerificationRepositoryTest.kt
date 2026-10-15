package com.appcoins.wallet.feature.walletInfo.data.verification

import com.appcoins.wallet.core.network.microservices.api.broker.BrokerVerificationApi
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import com.appcoins.wallet.sharedpreferences.BrokerVerificationPreferencesDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class BrokerVerificationRepositoryTest {

  private val wallet = "0xabc"
  private val api = mockk<BrokerVerificationApi>()
  private val prefs = mockk<BrokerVerificationPreferencesDataSource>(relaxed = true)
  private val walletInfoRepository = mockk<WalletInfoRepository> {
    every { getLatestWalletInfo(any()) } returns Single.just(mockk())
  }
  private val repository = BrokerVerificationRepository(walletInfoRepository, api, mockk(), prefs)

  private fun status(
    cached: VerificationStatus,
    server: Single<String>,
    type: VerificationType = VerificationType.PAYPAL,
  ): VerificationStatus {
    every { prefs.getCachedValidationStatus(wallet, type.ordinal) } returns cached.ordinal
    every { api.getVerificationState(any(), wallet) } returns server
    return repository.getVerificationStatus(wallet, type).blockingGet()
  }

  private fun http(code: Int) =
    Single.error<String>(HttpException(Response.error<String>(code, "".toResponseBody())))

  @Test
  fun `cached CODE_REQUESTED but server VERIFIED becomes VERIFIED and is cached`() {
    assertEquals(
      VerificationStatus.VERIFIED,
      status(VerificationStatus.CODE_REQUESTED, Single.just("VERIFIED"))
    )
    verify { api.getVerificationState("paypal", wallet) }
    verify {
      prefs.saveVerificationStatus(
        wallet, VerificationStatus.VERIFIED.ordinal, VerificationType.PAYPAL.ordinal
      )
    }
  }

  @Test
  fun `maps server states`() {
    mapOf(
      "VERIFIED" to VerificationStatus.VERIFIED,
      "PENDING_CODE" to VerificationStatus.CODE_REQUESTED,
      "PENDING_VALIDATION" to VerificationStatus.VERIFYING,
      "CANCELED" to VerificationStatus.UNVERIFIED,
      "EXPIRED" to VerificationStatus.UNVERIFIED,
      "FAILED" to VerificationStatus.UNVERIFIED,
    ).forEach { (server, expected) ->
      assertEquals(server, expected, status(VerificationStatus.VERIFYING, Single.just(server)))
    }
  }

  @Test
  fun `404 means never verified`() {
    assertEquals(VerificationStatus.UNVERIFIED, status(VerificationStatus.CODE_REQUESTED, http(404)))
  }

  @Test
  fun `network error keeps in-progress cached status`() {
    assertEquals(
      VerificationStatus.CODE_REQUESTED,
      status(VerificationStatus.CODE_REQUESTED, Single.error(IOException()))
    )
  }

  @Test
  fun `errors without in-progress cache map to NO_NETWORK or ERROR`() {
    assertEquals(
      VerificationStatus.NO_NETWORK,
      status(VerificationStatus.UNVERIFIED, Single.error(IOException()))
    )
    assertEquals(VerificationStatus.ERROR, status(VerificationStatus.UNVERIFIED, http(500)))
  }

  @Test
  fun `cached VERIFIED skips the server`() {
    assertEquals(
      VerificationStatus.VERIFIED,
      status(VerificationStatus.VERIFIED, Single.just("FAILED"))
    )
    verify(exactly = 0) { api.getVerificationState(any(), any()) }
  }

  @Test
  fun `credit card uses its own method path`() {
    status(
      VerificationStatus.CODE_REQUESTED,
      Single.just("PENDING_CODE"),
      VerificationType.CREDIT_CARD
    )
    verify { api.getVerificationState("credit_card", wallet) }
  }
}
