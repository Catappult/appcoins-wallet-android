package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.ui.login.usecases.GenerateWebLoginUrlUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

  private val hasWalletUseCase = mockk<HasWalletUseCase>()
  private val setOnboardingCompletedUseCase = mockk<SetOnboardingCompletedUseCase>(relaxed = true)
  private val walletsInteract = mockk<WalletsInteract>()
  private val generateWebLoginUrlUseCase = mockk<GenerateWebLoginUrlUseCase>()
  private val onboardingSignInWallet = OnboardingSignInWallet()
  private val getCurrentWalletUseCase = mockk<GetCurrentWalletUseCase> {
    every { this@mockk.invoke() } returns Single.just(Wallet("0xnew"))
  }
  private val schedulers = object : RxSchedulers {
    override val main = Schedulers.trampoline()
    override val io = Schedulers.trampoline()
    override val computation = Schedulers.trampoline()
  }

  @Before
  fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())

  @After
  fun tearDown() = Dispatchers.resetMain()

  private fun viewModel() = OnboardingViewModel(
    hasWalletUseCase = hasWalletUseCase,
    rxSchedulers = schedulers,
    setOnboardingCompletedUseCase = setOnboardingCompletedUseCase,
    recoverEntryPrivateKeyUseCase = mockk(),
    setDefaultWalletUseCase = mockk(),
    updateWalletInfoUseCase = mockk(),
    updateWalletNameUseCase = mockk(),
    getBonusGuestWalletUseCase = mockk(),
    deleteCachedGuestWalletUseCase = mockk(),
    walletsEventSender = mockk(relaxed = true),
    onboardingAnalytics = mockk(relaxed = true),
    saveIsFirstPaymentUseCase = mockk(relaxed = true),
    walletsInteract = walletsInteract,
    generateWebLoginUrlUseCase = generateWebLoginUrlUseCase,
    getCurrentWalletUseCase = getCurrentWalletUseCase,
    onboardingSignInWallet = onboardingSignInWallet,
    appStartUseCase = mockk<AppStartUseCase> {
      every { startModes } returns flowOf(StartMode.Regular)
    },
  )

  private fun OnboardingViewModel.sideEffects(count: Int) =
    runBlocking { sideEffectsFlow.take(count).toList() }

  @Test
  fun `sign in without a wallet creates one, completes onboarding and opens login`() {
    every { hasWalletUseCase() } returns Single.just(false)
    every { walletsInteract.createWallet("Main Wallet") } returns Completable.complete()
    every { generateWebLoginUrlUseCase() } returns Single.just("https://login")
    val vm = viewModel()

    vm.handleSignInClick()

    assertEquals(
      listOf(OnboardingSideEffect.ShowLoading, OnboardingSideEffect.OpenLogin("https://login")),
      vm.sideEffects(2)
    )
    verifyOrder {
      walletsInteract.createWallet("Main Wallet")
      setOnboardingCompletedUseCase()
      generateWebLoginUrlUseCase()
    }
    assertEquals("0xnew", onboardingSignInWallet.consume()) // remembered as temporary
  }

  @Test
  fun `sign in with an existing wallet does not create another`() {
    every { hasWalletUseCase() } returns Single.just(true)
    every { generateWebLoginUrlUseCase() } returns Single.just("https://login")
    val vm = viewModel()

    vm.handleSignInClick()

    assertEquals(OnboardingSideEffect.OpenLogin("https://login"), vm.sideEffects(2).last())
    verify(exactly = 0) { walletsInteract.createWallet(any()) }
    assertEquals(null, onboardingSignInWallet.consume()) // an existing wallet is never temporary
  }

  @Test
  fun `sign in failure shows an error`() {
    every { hasWalletUseCase() } returns Single.just(false)
    every { walletsInteract.createWallet(any()) } returns Completable.error(RuntimeException())
    val vm = viewModel()

    vm.handleSignInClick()

    assertEquals(OnboardingSideEffect.ShowSignInError, vm.sideEffects(2).last())
  }

  @Test
  fun `create local wallet asks for confirmation first`() {
    val vm = viewModel()

    vm.handleCreateLocalWalletClick()

    assertEquals(listOf(OnboardingSideEffect.ConfirmCreateLocalWallet), vm.sideEffects(1))
  }

  @Test
  fun `load from backup can go straight to the file picker`() {
    val vm = viewModel()

    vm.handleRecoverClick(openFilePicker = true)

    assertEquals(listOf(OnboardingSideEffect.NavigateToRecoverWallet(true)), vm.sideEffects(1))
  }
}
