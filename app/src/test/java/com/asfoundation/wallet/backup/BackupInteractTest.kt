package com.asfoundation.wallet.backup

import com.asfoundation.wallet.interact.FetchTransactionsInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
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
  lateinit var balanceInteractor: BalanceInteractor

  @Mock
  lateinit var gamificationInteractor: GamificationInteractor

  @Mock
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract

  private lateinit var backupInteract: BackupInteract

  @Before
  fun setup() {
    backupInteract =
        BackupInteract(sharedPreferencesRepository, fetchTransactionsInteract, balanceInteractor,
            gamificationInteractor, findDefaultWalletInteract)
  }

  @Test
  fun shouldShowSystemNotification_whenZeroPurchases_shouldReturnFalse() {
    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(0)
    `when`(sharedPreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
        .thenReturn(false)

    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)

    verify(sharedPreferencesRepository, times(0))
        .hasDismissedBackupSystemNotification(anyString())
    verify(sharedPreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)

    Assert.assertFalse(result)
  }

  @Test
  fun shouldShowSystemNotification_whenTwoPurchasesAndNotDismissed_shouldReturnTrue() {
    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(2)

    `when`(sharedPreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
        .thenReturn(false)

    `when`(sharedPreferencesRepository.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
        .thenReturn(false)

    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
    verify(sharedPreferencesRepository).hasDismissedBackupSystemNotification(WALLET_ADDRESS)
    verify(sharedPreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)

    Assert.assertTrue(result)
  }

  @Test
  fun shouldShowSystemNotification_whenTwoPurchasesAndDismissed_shouldReturnFalse() {
    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
        .thenReturn(2)

    `when`(sharedPreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
        .thenReturn(false)

    `when`(sharedPreferencesRepository.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
        .thenReturn(true)

    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)

    verify(sharedPreferencesRepository).hasDismissedBackupSystemNotification(WALLET_ADDRESS)
    verify(sharedPreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)

    Assert.assertFalse(result)
  }

  @Test
  fun updateWalletPurchasesCount() {
    val testObserver = TestObserver<Unit>()
    `when`(sharedPreferencesRepository.incrementWalletPurchasesCount(WALLET_ADDRESS, 1)).thenReturn(
        Completable.complete())

    backupInteract.updateWalletPurchasesCount(WALLET_ADDRESS)
        .subscribe(testObserver)

    testObserver.assertNoErrors()
        .assertComplete()
  }
}