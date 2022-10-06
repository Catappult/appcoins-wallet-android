package com.asfoundation.wallet.promotions.usecases

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.appcoins.wallet.gamification.repository.entity.VipReferralResponse
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorker
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorker.Companion.getUniqueName
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class VipReferralPollingUseCase @Inject constructor(
  private val getVipReferralUseCase: GetVipReferralUseCase,
  private val promotionsRepository: PromotionsRepository,
  private val repository: VipReferralPollingRepository,
  private val workManager: WorkManager,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(wallet: Wallet): Single<VipReferralResponse> {
    return getVipReferralUseCase(wallet)
      .doOnSuccess {
        if (it != VipReferralResponse.invalidReferral) {
          workManager.cancelUniqueWork(getUniqueName(wallet))
        }
      }
  }

  fun startPolling(wallet: Wallet): Completable =
    promotionsRepository.getGamificationStats(wallet.address, null)
      .subscribeOn(rxSchedulers.io)
      .doOnNext {
        val lastStatus =
          repository.getLastGamificationStatus().let(GamificationStatus.Companion::toEnum)
        if (lastStatus == GamificationStatus.APPROACHING_VIP && it.gamificationStatus == GamificationStatus.VIP) {
          workManager.enqueueUniqueWork(
            getUniqueName(wallet),
            ExistingWorkPolicy.KEEP,
            GetVipReferralWorker.getWorkRequest(
              wallet
            )
          )
        }
        repository.saveLastGamificationStatus(it.gamificationStatus.toString())
      }
      .doOnError {
        Log.d("WorkerManager", it.toString())
      }
      .ignoreElements()
}
