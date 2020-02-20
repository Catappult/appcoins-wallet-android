package com.asfoundation.wallet.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.asf.wallet.R
import com.asfoundation.wallet.advertise.WalletPoAService
import com.asfoundation.wallet.ui.TransactionsActivity
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject


class SupportNotificationWorker @Inject constructor(private val context: Context,
                                                    workerParams: WorkerParameters,
                                                    private val supportInteractor: SupportInteractor) :
    RxWorker(context, workerParams) {

  private var notificationManager: NotificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  private val channelId = "support_notification_channel_id"
  private val channelName: CharSequence = "Support notification channel"

  companion object {
    const val NOTIFICATION_PERIOD: Long = 10
    const val WORKER_TAG = "SupportNotificationWorkerTag"
    const val UNIQUE_WORKER_NAME = "SupportNotificationWorker"
  }

  @Module
  abstract class Builder {
    @Binds
    @IntoMap
    @WorkerKey(SupportNotificationWorker::class)
    abstract fun bindWorker(notificationWorker: SupportNotificationWorker): RxWorker
  }

  override fun createWork(): Single<Result> {
    return if (supportInteractor.shouldShowNotification()) {
      Completable.fromAction {
        notificationManager.notify(WalletPoAService.SERVICE_ID,
            createNotification().build())
      }
          .andThen(Single.just(Result.success()))
    } else {
      Single.just(Result.failure())
    }
  }

  private fun createNotification(): NotificationCompat.Builder {
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_LOW
      val notificationChannel =
          NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(context, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, channelId)
    }
    builder.setContentTitle("You have new messages")
    builder.setAutoCancel(true)
    builder.setContentIntent(createPendingIntent())
    return builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText("Click to open")
  }

  private fun createPendingIntent(): PendingIntent {
    val notifyIntent = Intent(context, TransactionsActivity::class.java)
    notifyIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    return PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT)
  }
}