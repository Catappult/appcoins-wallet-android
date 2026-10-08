package com.asfoundation.wallet.verification.ui.paypal

import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerificationPaypalViewModelTest {

  private val interactor = mockk<WalletVerificationInteractor>()
  private val setCachedVerificationUseCase = mockk<SetCachedVerificationUseCase>()
  private val getVerificationInfoUseCase = mockk<GetVerificationInfoUseCase> {
    every { this@mockk.invoke(any()) } returns Single.just(mockk(relaxed = true))
  }
  private val walletService = mockk<WalletService> {
    every { getAndSignCurrentWalletAddress() } returns Single.just(WalletAddressModel("0x", "sig"))
  }

  @Before
  fun setup() = RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

  @After
  fun tearDown() = RxJavaPlugins.reset()

  private fun viewModel(initialStatus: VerificationStatus): VerificationPaypalViewModel {
    serverStatus(initialStatus)
    return VerificationPaypalViewModel(
      getVerificationInfoUseCase,
      mockk(relaxed = true),
      setCachedVerificationUseCase,
      mockk(relaxed = true),
      interactor,
      walletService,
      mockk(relaxed = true)
    )
  }

  private fun serverStatus(status: VerificationStatus) {
    every { interactor.getVerificationStatus("0x", VerificationType.PAYPAL) } returns
        Single.just(status)
  }

  @Test
  fun `webview success shows check your email instead of the code input`() {
    every {
      setCachedVerificationUseCase(VerificationStatus.VERIFYING, VerificationType.PAYPAL)
    } returns Completable.complete()
    val vm = viewModel(VerificationStatus.UNVERIFIED)
    assertTrue(vm.uiState.value is VerificationPaypalState.ShowVerificationInfo)

    vm.successPayment()

    assertEquals(VerificationPaypalState.CheckEmail, vm.uiState.value)
  }

  @Test
  fun `reopening a pending verification lands on check your email`() {
    assertEquals(
      VerificationPaypalState.CheckEmail,
      viewModel(VerificationStatus.CODE_REQUESTED).uiState.value
    )
  }

  @Test
  fun `refresh with server VERIFIED completes the verification`() {
    val vm = viewModel(VerificationStatus.CODE_REQUESTED)
    serverStatus(VerificationStatus.VERIFIED)

    vm.refreshEmailVerification()

    assertEquals(VerificationPaypalState.VerificationCompleted, vm.uiState.value)
  }

  @Test
  fun `refresh with server canceled, expired, failed or 404 shows the error`() {
    val vm = viewModel(VerificationStatus.CODE_REQUESTED)
    serverStatus(VerificationStatus.UNVERIFIED)

    vm.refreshEmailVerification()

    assertEquals(VerificationPaypalState.UnknownError, vm.uiState.value)
  }

  @Test
  fun `refresh while still pending or offline keeps waiting`() {
    val vm = viewModel(VerificationStatus.CODE_REQUESTED)
    serverStatus(VerificationStatus.NO_NETWORK)

    vm.refreshEmailVerification()

    assertEquals(VerificationPaypalState.CheckEmail, vm.uiState.value)
  }
}
