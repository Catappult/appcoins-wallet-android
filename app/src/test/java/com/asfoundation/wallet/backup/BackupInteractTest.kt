package com.asfoundation.wallet.backup

//import com.asfoundation.wallet.backup.repository.preferences.BackupSystemNotificationPreferences
//import com.asfoundation.wallet.home.usecases.FetchTransactionsUseCase
//import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
//import com.asfoundation.wallet.repository.PreferencesRepositoryType
//import com.appcoins.wallet.feature.walletInfo.data.balance.BalanceInteractor
//import com.asfoundation.wallet.ui.gamification.GamificationInteractor
//import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
//import com.appcoins.wallet.feature.walletInfo.data.GetWalletInfoUseCase
//import io.reactivex.Completable
//import io.reactivex.observers.TestObserver
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.ArgumentMatchers.anyString
//import org.mockito.Mock
//import org.mockito.Mockito.*
//import org.mockito.junit.MockitoJUnitRunner
//
//@RunWith(MockitoJUnitRunner::class)
//class BackupInteractTest {
//
//  companion object {
//    private const val WALLET_ADDRESS = "a_wallet_address"
//  }
//
//  @Mock
//  lateinit var sharedPreferencesRepository: PreferencesRepositoryType
//
//  @Mock
//  lateinit var fetchTransactionsUseCase: FetchTransactionsUseCase
//
//  @Mock
//  lateinit var backupRestorePreferencesRepository: BackupRestorePreferencesRepository
//
//  @Mock
//  lateinit var backupSystemNotificationPreferences: BackupSystemNotificationPreferences
//
//  @Mock
//  lateinit var balanceInteractor: BalanceInteractor
//
//  @Mock
//  lateinit var gamificationInteractor: GamificationInteractor
//
//  @Mock
//  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
//
//  @Mock
//  lateinit var getWalletInfoUseCase: GetWalletInfoUseCase
//
//  private lateinit var backupInteract: BackupInteract
//
//  @Before
//  fun setup() {
//    backupInteract =
//        BackupInteract(sharedPreferencesRepository, backupRestorePreferencesRepository, backupSystemNotificationPreferences,
//            fetchTransactionsUseCase, getWalletInfoUseCase, gamificationInteractor,
//            findDefaultWalletInteract)
//  }
//
//  @Test
//  fun shouldShowSystemNotification_whenZeroPurchases_shouldReturnFalse() {
//    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
//        .thenReturn(0)
//    `when`(backupRestorePreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
//        .thenReturn(false)
//
//    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
//
//    verify(backupSystemNotificationPreferences, times(0))
//        .hasDismissedBackupSystemNotification(anyString())
//    verify(backupRestorePreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)
//
//    Assert.assertFalse(result)
//  }
//
//  @Test
//  fun shouldShowSystemNotification_whenTwoPurchasesAndNotDismissed_shouldReturnTrue() {
//    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
//        .thenReturn(2)
//
//    `when`(backupRestorePreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
//        .thenReturn(false)
//
//    `when`(backupSystemNotificationPreferences.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
//        .thenReturn(false)
//
//    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
//    verify(backupSystemNotificationPreferences).hasDismissedBackupSystemNotification(WALLET_ADDRESS)
//    verify(backupRestorePreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)
//
//    Assert.assertTrue(result)
//  }
//
//  @Test
//  fun shouldShowSystemNotification_whenTwoPurchasesAndDismissed_shouldReturnFalse() {
//    `when`(sharedPreferencesRepository.getWalletPurchasesCount(WALLET_ADDRESS))
//        .thenReturn(2)
//
//    `when`(backupRestorePreferencesRepository.isWalletRestoreBackup(WALLET_ADDRESS))
//        .thenReturn(false)
//
//    `when`(backupSystemNotificationPreferences.hasDismissedBackupSystemNotification(WALLET_ADDRESS))
//        .thenReturn(true)
//
//    val result = backupInteract.shouldShowSystemNotification(WALLET_ADDRESS)
//
//    verify(backupSystemNotificationPreferences).hasDismissedBackupSystemNotification(WALLET_ADDRESS)
//    verify(backupRestorePreferencesRepository).isWalletRestoreBackup(WALLET_ADDRESS)
//
//    Assert.assertFalse(result)
//  }
//
//  @Test
//  fun updateWalletPurchasesCount() {
//    val testObserver = TestObserver<Unit>()
//    `when`(sharedPreferencesRepository.incrementWalletPurchasesCount(WALLET_ADDRESS, 1)).thenReturn(
//        Completable.complete())
//
//    backupInteract.updateWalletPurchasesCount(WALLET_ADDRESS)
//        .subscribe(testObserver)
//
//    testObserver.assertNoErrors()
//        .assertComplete()
//  }
//}