package com.asfoundation.wallet.promotions.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.promotions.alarm.NotificationScheduler
import com.asfoundation.wallet.promotions.usecases.GetVipReferralUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class GetVipReferralWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val getVipReferralUseCase: GetVipReferralUseCase,
  private val getCurrentWallet: GetCurrentWalletUseCase,
  private val promotionsRepository: PromotionsRepository,
  private val rxSchedulers: RxSchedulers,
  private val notificationScheduler: NotificationScheduler,
) : RxWorker(context, params) {

  override fun getBackgroundScheduler() = rxSchedulers.io

  override fun createWork(): Single<Result> =
    getCurrentWallet()
      .map { shouldStartPolling(it) }
      .flatMap { it }
      .flatMapCompletable { pair ->
        if (pair.second) {
          promotionsRepository.setReferralNotificationSeen(pair.first.address, true)
          scheduleNotification(pair.first)
        } else {
          Completable.complete()
        }
      }
      .andThen(Single.just(Result.success()))
      .subscribeOn(backgroundScheduler)

  private fun shouldStartPolling(wallet: Wallet): Single<Pair<Wallet, Boolean>> =
    promotionsRepository.isReferralNotificationToShow(wallet.address)
      .firstOrError()
      .map { wallet to it }

  private fun scheduleNotification(wallet: Wallet) =
    getVipReferralUseCase(wallet)
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

    fun getUniqueName(wallet: Wallet): String = "$NAME#${wallet.address}"

    fun getWorkRequest(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<GetVipReferralWorker>()
        .setInitialDelay(INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
  }

  @AssistedFactory
  interface Factory {
    fun create(appContext: Context, params: WorkerParameters): GetVipReferralWorker
  }
}
