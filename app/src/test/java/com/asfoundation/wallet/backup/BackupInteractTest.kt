package com.asfoundation.wallet.backup

import com.asfoundation.wallet.interact.FetchTransactionsInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BackupInteractTest {

  companion object {
    private const val WALLET_ADDRESS = "a_wallet_address"
  }

  @Mock
  lateinit var sharedPreferencesRepository: PreferencesRepositoryType

  @Mock
  lateinit var fetchTransactionsInteract: FetchTransactionsInteract

  @Mock
  lateinit var balanceInteract: BalanceInteract

  @Mock
  lateinit var gamificationInteractor: GamificationInteractor

  @Mock
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract

  private lateinit var backupInteract: BackupInteract

  @Before
  fun setup() {
    backupInteract =
        BackupInteract(sharedPreferencesRepository, fetchTransactionsInteract, balanceInteract,
            gamificationInteractor, findDefaultWalletInteract)
  }

  @Test
  fun shouldShowSystemNotification_whenZeroPurchases_shouldReturnFalse() {
    val testObserver = TestObserver<Boolean>()

    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(Single.just(0))

    backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
        .subscribe(testObserver)

    verify(sharedPreferencesRepository, times(0))
        .hasDismissedBackupSystemNotification(anyString())

    testObserver.assertNoErrors()
        .assertValue(false)
  }

  @Test
  fun shouldShowSystemNotification_whenTwoPurchasesAndNotDismissed_shouldReturnTrue() {
    val testObserver = TestObserver<Boolean>()

    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(Single.just(2))

    `when`(sharedPreferencesRepository.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
        .thenReturn(Single.just(false))

    backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
        .subscribe(testObserver)

    verify(sharedPreferencesRepository).hasDismissedBackupSystemNotification(WALLET_ADDRESS)

    testObserver.assertNoErrors()
        .assertValue(true)
  }

  @Test
  fun shouldShowSystemNotification_whenTwoPurchasesAndDismissed_shouldReturnFalse() {
    val testObserver = TestObserver<Boolean>()

    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(Single.just(2))

    `when`(sharedPreferencesRepository.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
        .thenReturn(Single.just(true))

    backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
        .subscribe(testObserver)

    verify(sharedPreferencesRepository).hasDismissedBackupSystemNotification(WALLET_ADDRESS)

    testObserver.assertNoErrors()
        .assertValue(false)
  }

  @Test
  fun updateWalletPurchasesCount() {
    val testObserver = TestObserver<Unit>()
    `when`(sharedPreferencesRepository.incrementWalletPurchasesCount(WALLET_ADDRESS)).thenReturn(
        Completable.complete())

    backupInteract.updateWalletPurchasesCount(WALLET_ADDRESS)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertComplete()
  }
}