package com.asfoundation.wallet.promotions.worker

import android.content.Context
import androidx.work.*
import com.appcoins.wallet.gamification.repository.entity.VipReferralResponse.Companion.invalidReferral
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.promotions.usecases.GetVipReferralUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetVipReferralWorker(
  context: Context,
  params: WorkerParameters
) : RxWorker(context, params) {

  @Inject
  lateinit var getVipReferralUseCase: GetVipReferralUseCase

  @Inject
  lateinit var getCurrentWallet: GetCurrentWalletUseCase

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  override fun getBackgroundScheduler() = rxSchedulers.io

  override fun createWork(): Single<Result> = getCurrentWallet()
    .flatMap(getVipReferralUseCase::invoke)
    .filter { it != invalidReferral }
    .map { Result.success() }
    .toSingle()
    .onErrorReturn {
      if (runAttemptCount > RETRY_COUNTS) {
        Result.failure()
      } else {
        Result.retry()
      }
    }

  companion object {
    const val NAME = "com.asfoundation.wallet.promotions.worker.GetVipReferralWorker"
    private const val RETRY_MINUTES = 5L
    private const val RETRY_COUNTS = 24

    val workRequest by lazy {
      OneTimeWorkRequestBuilder<GetVipReferralWorker>()
        .setInitialDelay(RETRY_MINUTES, TimeUnit.MINUTES)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
        .build()
    }
  }
}
