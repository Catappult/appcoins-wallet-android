package com.asfoundation.wallet.service

import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.feature.walletInfo.data.AccountKeystoreService
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletCreatorInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AccountWalletServiceTest {

  @Mock
  lateinit var accountKeyService: AccountKeystoreService

  @Mock
  lateinit var passwordStore: PasswordStore

  @Mock
  lateinit var walletCreatorInteract: WalletCreatorInteract

  @Mock
  lateinit var walletRepository: WalletRepositoryType

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
    `when`(walletRepository.getDefaultWallet()).thenReturn(
        Single.just(Wallet(ADDRESS)))
    `when`(passwordStore.getPassword(any())).thenReturn(Single.just(PASSWORD))
    `when`(accountKeyService.exportAccount(any(), any(), any())).thenReturn(Single.just(KEYSTORE))

    accountWalletService = AccountWalletService(accountKeyService,
            passwordStore, walletCreatorInteract,
            walletRepository, syncScheduler)
  }

  @Test
  fun signContent() {
    val testObserver = TestObserver<String>()
    accountWalletService.signContent(ADDRESS)
        .subscribe(testObserver)
    testObserver.assertNoErrors()
        .assertValue(
            "c7a5c8192cf90952b0cd492ab2fd0d41d96e2e158c365b92742eb5e1bcbf70885a7947331d92717ccc8b938d9f725e541cae8fb4360424533bb86b20d829dc5b00")
  }

  @Test
  fun signContentWithoutAddress() {
    val testObserver = TestObserver<WalletAddressModel>()
    accountWalletService.getAndSignCurrentWalletAddress()
        .subscribe(testObserver)
    testObserver.assertNoErrors()
        .assertValue(
            WalletAddressModel(ADDRESS,
                "c7a5c8192cf90952b0cd492ab2fd0d41d96e2e158c365b92742eb5e1bcbf70885a7947331d92717ccc8b938d9f725e541cae8fb4360424533bb86b20d829dc5b00"))
  }
}