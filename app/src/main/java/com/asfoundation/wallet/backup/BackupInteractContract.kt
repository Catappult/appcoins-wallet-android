package com.asfoundation.wallet.backup

import com.asfoundation.wallet.referrals.CardNotification
import io.reactivex.Completable
import io.reactivex.Single

interface BackupInteractContract {

  fun getUnwatchedBackupNotification(): Single<CardNotification>

  fun dismissNotification(): Completable

  fun shouldShowSystemNotification(walletAddress: String): Boolean

  fun updateWalletPurchasesCount(walletAddress: String): Completable

  fun saveDismissSystemNotification(walletAddress: String)
}