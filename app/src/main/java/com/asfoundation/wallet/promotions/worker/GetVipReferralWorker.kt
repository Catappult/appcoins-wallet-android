package com.asfoundation.wallet.promotions.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.promotions.alarm.NotificationScheduler
import com.asfoundation.wallet.promotions.usecases.GetVipReferralUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.Date
import java.util.concurrent.TimeUnit

class GetVipReferralWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val getVipReferralUseCase: GetVipReferralUseCase,
  private val getCurrentWallet: GetCurrentWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val notificationScheduler: NotificationScheduler,
) : RxWorker(context, params) {

  override fun getBackgroundScheduler() = rxSchedulers.io

  override fun createWork(): Single<Result> =
    getCurrentWallet()
      .flatMapCompletable { scheduleNotification(it) }
      .andThen(Single.just(Result.success()))
      .subscribeOn(backgroundScheduler)

  private fun scheduleNotification(wallet: Wallet) =
    getVipReferralUseCase(wallet)
      .filter { (it.startDateAsDate?.compareTo(Date()) ?: 0) > 0 }
      .flatMapCompletable {
        notificationScheduler.scheduleNotification(
          walletAddress = wallet.address,
          date = it.startDateAsDate,
          vipReferralCode = it.code
        )
      }

  companion object {
    private const val NAME = "GetVipReferralWorker"
    private const val INITIAL_DELAY_SECONDS = 1L

    private fun getUniqueName(wallet: Wallet): String = "$NAME#${wallet.address}"

    private fun getWorkRequest(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<GetVipReferralWorker>()
        .setInitialDelay(INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()

    fun cancelUniqueWork(workManager: WorkManager, wallet: Wallet) =
      workManager.cancelUniqueWork(getUniqueName(wallet))

    fun enqueueUniqueWork(workManager: WorkManager, wallet: Wallet) =
      workManager.enqueueUniqueWork(
        getUniqueName(wallet),
        ExistingWorkPolicy.REPLACE,
        getWorkRequest()
      )
  }

  @AssistedFactory
  interface Factory {
    fun create(appContext: Context, params: WorkerParameters): GetVipReferralWorker
  }
}
