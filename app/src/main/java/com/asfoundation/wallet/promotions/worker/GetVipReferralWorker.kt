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
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.asfoundation.wallet.promotions.usecases.GetVipReferralUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetVipReferralWorker(
  private val context: Context,
  params: WorkerParameters
) : RxWorker(context, params) {

  @Inject
  lateinit var getVipReferralUseCase: GetVipReferralUseCase

  @Inject
  lateinit var getCurrentWallet: GetCurrentWalletUseCase

  @Inject
  lateinit var pendingIntentNavigator: PendingIntentNavigator

  @Inject
  lateinit var notificationManager: NotificationManager

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  override fun getBackgroundScheduler() = rxSchedulers.io

  override fun createWork(): Single<Result> = getCurrentWallet()
    .flatMap(getVipReferralUseCase::invoke)
    .filter { it != invalidReferral }
    .doOnSuccess { showNotification(it.code) }
    .map { Result.success() }
    .toSingle()
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
    const val NAME = "com.asfoundation.wallet.promotions.worker.GetVipReferralWorker"
    private const val RETRY_MINUTES = 5L
    private const val RETRY_COUNTS = 24
    private const val CHANNEL_NAME = "VIP Referral Notification Channel"
    private const val CHANNEL_ID = "notification_channel_vip_referral"
    private const val NOTIFICATION_SERVICE_ID = 77777

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
