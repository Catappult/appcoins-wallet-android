package com.asfoundation.wallet.promotions.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ShareCompat
import androidx.work.*
import com.appcoins.wallet.gamification.repository.entity.VipReferralResponse.Companion.invalidReferral
import com.asf.wallet.R
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.asfoundation.wallet.promotions.usecases.GetVipReferralUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetVipReferralWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val getVipReferralUseCase: GetVipReferralUseCase,
  private val getCurrentWallet: GetCurrentWalletUseCase,
  private val pendingIntentNavigator: PendingIntentNavigator,
  private val notificationManager: NotificationManager,
  private val rxSchedulers: RxSchedulers

) : RxWorker(context, params) {

  override fun getBackgroundScheduler() = rxSchedulers.io

  override fun createWork(): Single<Result> = getCurrentWallet()
    .filter { it.address == inputData.getString(ADDRESS_DATA_KEY) }
    .flatMapSingle(getVipReferralUseCase::invoke)
    .filter { it.active && it.code.isNotEmpty() }
    .toSingle()
    .map {
      showNotification(it.code)
      Result.success()
    }
    .onErrorReturn {
      if (runAttemptCount > RETRY_COUNTS) {
        Result.failure()
      } else {
        Result.retry()
      }
    }

  private fun showNotification(code: String) = notificationManager.notify(
    NOTIFICATION_SERVICE_ID,
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setAutoCancel(true)
      .setContentIntent(pendingIntentNavigator.getPromotionsPendingIntent())
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setContentTitle(context.getString(R.string.vip_program_referral_notification_title))
      .setContentText(context.getString(R.string.vip_program_referral_notification_body))
      .addAction(
        android.R.drawable.ic_menu_share,
        context.getString(R.string.wallet_view_share_button),
        getSharePendingIntent(code)
      )
      .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
          )
        } else {
          setVibrate(LongArray(0))
        }
      }
      .build(),
  )

  private fun getSharePendingIntent(code: String) = PendingIntent.getActivity(
    context,
    0,
    ShareCompat.IntentBuilder(context)
      .setText(code)
      .setType("text/plain")
      .setChooserTitle(context.getString(R.string.share_via))
      .intent,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }
  )


  companion object {
    private const val NAME = "GetVipReferralWorker"
    private const val INITIAL_DELAY_MINUTES = 1L
    private const val RETRY_MINUTES = 5L
    private const val RETRY_COUNTS = 24
    private const val CHANNEL_NAME = "VIP Referral Notification Channel"
    private const val CHANNEL_ID = "notification_channel_vip_referral"
    private const val NOTIFICATION_SERVICE_ID = 77777
    private const val ADDRESS_DATA_KEY = "address"

    fun getUniqueName(wallet: Wallet): String = "$NAME#${wallet.address}"

    fun getWorkRequest(wallet: Wallet): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<GetVipReferralWorker>()
        .setInitialDelay(INITIAL_DELAY_MINUTES, TimeUnit.MINUTES)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, RETRY_MINUTES, TimeUnit.MINUTES)
        .setInputData(workDataOf(
            ADDRESS_DATA_KEY to wallet.address
          )
        )
        .build()
  }

  @AssistedFactory
  interface Factory {
    fun create(appContext: Context, params: WorkerParameters): GetVipReferralWorker
  }

}
