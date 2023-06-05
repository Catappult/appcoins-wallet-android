package com.asfoundation.wallet.promotions.usecases

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorker
import io.reactivex.Completable
import javax.inject.Inject

class StartVipReferralPollingUseCase  @Inject constructor(
  private val promotionsRepository: PromotionsRepository,
  private val workManager: WorkManager,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(wallet: Wallet): Completable {
    return promotionsRepository.isReferralNotificationToShow(wallet.address)
      .subscribeOn(rxSchedulers.io)
      .doOnNext { isToStartPolling ->
        if (isToStartPolling) {
          promotionsRepository.setReferralNotificationSeen(wallet.address, true)
          workManager.enqueueUniqueWork(
            GetVipReferralWorker.getUniqueName(wallet),
            ExistingWorkPolicy.KEEP,
            GetVipReferralWorker.getWorkRequest(
              wallet
            )
          )
        }
      }
      .doOnError {
        Log.d("WorkerManager", it.toString())
      }
      .ignoreElements()
  }

}
