package com.asfoundation.wallet.ui.login.usecases

import com.appcoins.wallet.core.network.backend.model.FetchUserKeyResponse
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.asfoundation.wallet.interact.DeleteWalletInteract
import com.asfoundation.wallet.onboarding.OnboardingSignInWallet
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.WalletSwitched
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FetchUserKeyUseCaseTest {

  private val temporaryWallet = "0xtemp"
  private val accountWallet = "0xaccount"
  private val onboardingSignInWallet = OnboardingSignInWallet()
  private val deleteWalletInteract = mockk<DeleteWalletInteract> {
    every { delete(any()) } returns Completable.complete()
  }
  private val setActiveWalletUseCase = mockk<SetActiveWalletUseCase> {
    every { this@mockk.invoke(any<String>()) } returns Completable.complete()
  }

  private fun useCase(
    currentWallet: String,
    recoverResult: RecoverEntryResult = SuccessfulEntryRecover(accountWallet, null),
  ) = FetchUserKeyUseCase(
    loginRepository = mockk { every { fetchUserKey("token") } returns Single.just(FetchUserKeyResponse("key")) },
    recoverEntryPrivateKeyUseCase = mockk {
      every { this@mockk.invoke(any()) } returns Single.just(recoverResult)
    },
    updateWalletInfoUseCase = mockk {
      every { this@mockk.invoke(any(), any()) } returns Completable.complete()
      every { this@mockk.invoke(any()) } returns Completable.complete()
    },
    setOnboardingCompletedUseCase = mockk(relaxed = true),
    updateWalletNameUseCase = mockk { every { this@mockk.invoke(any(), any()) } returns Completable.complete() },
    setActiveWalletUseCase = setActiveWalletUseCase,
    getWalletInfoUseCase = mockk {
      every { this@mockk.invoke(null, true) } returns Single.just(
        mockk<WalletInfo> {
          every { wallet } returns currentWallet
          every { email } returns null
        }
      )
    },
    getAddressFromPrivateKeyUseCase = mockk { every { this@mockk.invoke("key") } returns Single.just(accountWallet) },
    onboardingSignInWallet = onboardingSignInWallet,
    deleteWalletInteract = deleteWalletInteract,
  )

  @Test
  fun `signing in to an existing account removes the temporary onboarding wallet`() {
    onboardingSignInWallet.set(temporaryWallet)

    val result = useCase(currentWallet = temporaryWallet)("token", "a@b.c").blockingGet()

    assertEquals(WalletSwitched("a@b.c"), result)
    verify { deleteWalletInteract.delete(temporaryWallet) }
    verify { setActiveWalletUseCase(accountWallet) }
    assertNull(onboardingSignInWallet.consume())
  }

  @Test
  fun `a wallet not created by onboarding sign-in is never removed`() {
    useCase(currentWallet = "0xmine")("token", "a@b.c").blockingGet()

    verify(exactly = 0) { deleteWalletInteract.delete(any()) }
  }

  @Test
  fun `an expired temporary wallet is kept`() {
    onboardingSignInWallet.set(temporaryWallet, now = 0L)

    useCase(currentWallet = temporaryWallet)("token", "a@b.c").blockingGet()

    verify(exactly = 0) { deleteWalletInteract.delete(any()) }
  }

  @Test
  fun `a failed login keeps the temporary wallet for a retry`() {
    onboardingSignInWallet.set(temporaryWallet)

    useCase(currentWallet = temporaryWallet, recoverResult = FailedEntryRecover.GenericError())(
      "token", "a@b.c"
    ).blockingGet()

    verify(exactly = 0) { deleteWalletInteract.delete(any()) }
    assertEquals(temporaryWallet, onboardingSignInWallet.consume())
  }
}
