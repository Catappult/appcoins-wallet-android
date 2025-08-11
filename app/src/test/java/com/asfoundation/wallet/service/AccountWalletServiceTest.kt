package com.asfoundation.wallet.service

import android.content.Context
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.appcoins.wallet.feature.walletInfo.data.AccountKeystoreService
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.CreateWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetPrivateKeyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RecoverEntryPrivateKeyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SignUseCase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AccountWalletServiceTest {

  private val signUseCase = SignUseCase()

  @Mock
  lateinit var context: Context

  @Mock
  lateinit var accountKeyService: AccountKeystoreService

  @Mock
  lateinit var passwordStore: PasswordStore

  @Mock
  lateinit var getCurrentWalletUseCase: GetCurrentWalletUseCase

  private lateinit var getPrivateKeyUseCase: GetPrivateKeyUseCase

  @Mock
  lateinit var createWalletUseCase: CreateWalletUseCase

  @Mock
  lateinit var registerFirebaseTokenUseCase: RegisterFirebaseTokenUseCase

  @Mock
  lateinit var recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase

  @Mock
  lateinit var walletRepositoryType: WalletRepositoryType

  @Mock
  lateinit var syncScheduler: ExecutorScheduler

  private lateinit var accountWalletService: AccountWalletService

  companion object {
    const val KEYSTORE =
      "{\"address\":\"8f91a6405399360d3b57569174d09808eb86496f\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"68f3c5cbd61c6b6736cc4f62bf3d546a7b8f75cad35b5a44eea7f0a4174f5570\",\"cipherparams\":{\"iv\":\"801d940387046cfffec86ddb0d540f8e\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":4096,\"p\":6,\"r\":8,\"salt\":\"6a7cebc7e4d943e87a344581b21b0c6eee95f5072e55c944bbb57d8eeb2dfef0\"},\"mac\":\"583cbadb6e3fc1b93b8e3626632b71cf6c6277d93d866579abd7a8c432d0ddd3\"},\"id\":\"c411f666-7af9-49f3-965a-75cbcc233b5e\",\"version\":3}"
    const val ADDRESS = "0x8F91A6405399360d3B57569174D09808Eb86496f"
    const val PASSWORD = "appcoins"
  }

  @Before
  fun setUp() {
    `when`(passwordStore.getPassword(any())).thenReturn(Single.just(PASSWORD))
    `when`(accountKeyService.exportAccount(any(), any(), any())).thenReturn(Single.just(KEYSTORE))
    `when`(getCurrentWalletUseCase()).thenReturn(Single.just(Wallet(ADDRESS)))

    getPrivateKeyUseCase = GetPrivateKeyUseCase(
      accountKeyService = accountKeyService,
      passwordStore = passwordStore
    )

    accountWalletService = AccountWalletService(
      passwordStore = passwordStore,
      walletRepository = walletRepositoryType,
      syncScheduler = syncScheduler,
      registerFirebaseTokenUseCase = registerFirebaseTokenUseCase,
      getPrivateKeyUseCase = getPrivateKeyUseCase,
      signUseCase = signUseCase,
      createWalletUseCase = createWalletUseCase,
      getCurrentWalletUseCase = getCurrentWalletUseCase,
      context = context,
      recoverEntryPrivateKeyUseCase = recoverEntryPrivateKeyUseCase,
    )
  }

  @Test
  fun signContent() {
    val testObserver = TestObserver<String>()
    accountWalletService.signContent(ADDRESS)
      .subscribe(testObserver)
    testObserver.assertNoErrors()
      .assertValue(
        "c7a5c8192cf90952b0cd492ab2fd0d41d96e2e158c365b92742eb5e1bcbf70885a7947331d92717ccc8b938d9f725e541cae8fb4360424533bb86b20d829dc5b00"
      )
  }

  @Test
  fun signContentWithoutAddress() {
    val testObserver = TestObserver<WalletAddressModel>()
    accountWalletService.getAndSignCurrentWalletAddress()
      .subscribe(testObserver)
    testObserver.assertNoErrors()
      .assertValue(
        WalletAddressModel(
          ADDRESS,
          "c7a5c8192cf90952b0cd492ab2fd0d41d96e2e158c365b92742eb5e1bcbf70885a7947331d92717ccc8b938d9f725e541cae8fb4360424533bb86b20d829dc5b00"
        )
      )
  }
}