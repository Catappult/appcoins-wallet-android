package com.asfoundation.wallet.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.asf.wallet.R
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

  companion object {
    const val NOTIFICATION_SERVICE_ID = 77794
    const val NOTIFICATION_PERIOD: Long = 15
    const val WORKER_TAG = "SupportNotificationWorkerTag"
    const val UNIQUE_WORKER_NAME = "SupportNotificationWorker"
    const val channelId = "support_notification_channel_id"
    const val channelName = "Support notification channel"
    const val UNREAD_CONVERSATIONS = "UNREAD_CONVERSATIONS"
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
      supportInteractor.updateUnreadConversations()
      Completable.fromAction {
        notificationManager.notify(NOTIFICATION_SERVICE_ID,
            createNotification().build())
      }
          .andThen(Single.just(Result.success()))
    } else {
      Single.just(Result.failure())
    }
  }

  private fun createNotification(): NotificationCompat.Builder {
    val okPendingIntent = createNotificationClickIntent()
    val dismissPendingIntent = createNotificationDismissIntent()
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel =
          NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(context, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, channelId)
    }
    return builder.setContentTitle("You have new messages")
        .setAutoCancel(true)
        .setOngoing(true)
        .setContentIntent(okPendingIntent)
        .addAction(0, "Dismiss", dismissPendingIntent)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText("Click to open")
  }

  private fun createNotificationClickIntent(): PendingIntent {
    val intent = SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(SupportNotificationBroadcastReceiver.ACTION_KEY,
        SupportNotificationBroadcastReceiver.ACTION_CHECK_MESSAGES)
    return PendingIntent.getBroadcast(context, 0, intent, 0)
  }

  private fun createNotificationDismissIntent(): PendingIntent {
    val intent = SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(SupportNotificationBroadcastReceiver.ACTION_KEY,
        SupportNotificationBroadcastReceiver.ACTION_DISMISS)
    return PendingIntent.getBroadcast(context, 1, intent, 0)
  }
}

