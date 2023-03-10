package cm.aptoide.skills.games

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import cm.aptoide.skills.R
import cm.aptoide.skills.repository.RoomRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundGameService : Service(), GameStateListener {
  companion object {
    private const val NOTIFICATION_SERVICE_ID = 77798
    private const val CHANNEL_ID = "game_notification_channel_id"
    private const val CHANNEL_NAME = "Game Notification Channel"

    private const val ACTION_DISMISS = "ACTION_DISMISS"
    private const val SESSION = "SESSION"
    private const val GET_ROOM_PERIOD_SECONDS = 3L
    private const val PLAYER_SEPARATOR = " vs "

    @JvmStatic
    fun newIntent(context: Context, sessionToken: String) =
      Intent(context, BackgroundGameService::class.java).apply {
        putExtra(SESSION, sessionToken)
      }
  }

  @Inject
  lateinit var roomRepository: RoomRepository

  private lateinit var notificationManager: NotificationManager
  private lateinit var periodicGameChecker: PeriodicGameChecker

  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()
    notificationManager = getNotificationManager()
    periodicGameChecker = PeriodicGameChecker(roomRepository, GET_ROOM_PERIOD_SECONDS, this)
  }

  private fun getNotificationManager(): NotificationManager {
    val notificationManager =
      this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_LOW
      val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
      notificationManager.createNotificationChannel(notificationChannel)
    }
    return notificationManager
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_DISMISS -> stopService()
    }

    val session: String? = intent?.getStringExtra(SESSION)
    session?.let {
      periodicGameChecker.start(it)
      val notification = getNotification(
        getString(R.string.playing_game_notification_title),
        getString(R.string.playing_game_notification_body)
      )
      this.startForeground(NOTIFICATION_SERVICE_ID, notification)
    }
    return super.onStartCommand(intent, flags, startId)
  }

  fun getNotification(title: String, text: String): Notification {
    val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
    val dismissIntent = createNotificationDismissIntent()
    return notificationBuilder.setContentTitle(title)
      .addAction(0, getString(R.string.dismiss_button), dismissIntent)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setDeleteIntent(dismissIntent)
      .setContentText(text)
      .build()
  }

  private fun createNotificationDismissIntent(): PendingIntent {
    val intent = Intent(this, BackgroundGameService::class.java)
    intent.action = ACTION_DISMISS
    return PendingIntent.getService(
      this,
      0,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      else
        PendingIntent.FLAG_UPDATE_CURRENT
    )
  }

  override fun onUpdate(gameUpdate: GameUpdate) {
    val notification = getNotification(
      getString(R.string.playing_game_notification_title),
      getFormattedGameDetails(gameUpdate)
    )
    notificationManager.notify(NOTIFICATION_SERVICE_ID, notification)
  }

  private fun getFormattedGameDetails(gameUpdate: GameUpdate): String {
    val gameDetails = StringBuilder()
    for (i in gameUpdate.userNames.indices) {
      val userName: String = gameUpdate.userNames[i]
      gameDetails.append(userName)
      if (i < gameUpdate.userNames.size - 1) {
        gameDetails.append(PLAYER_SEPARATOR)
      }
    }
    return gameDetails.toString()
  }

  override fun onFinishGame(finishedGame: FinishedGame) {
    periodicGameChecker.stop()
    val notification: Notification = if (finishedGame.isWinner) {
      getNotification(
        getString(R.string.finish_game_notification_title),
        getString(R.string.won_game_notification_body, finishedGame.winnerAmount)
      )
    } else {
      getNotification(
        getString(R.string.finish_game_notification_title),
        getString(R.string.lost_game_notification_body)
      )
    }

    notificationManager.notify(NOTIFICATION_SERVICE_ID, notification)
    stopForeground(false)
  }

  private fun stopService() {
    periodicGameChecker.stop()
    notificationManager.cancel(NOTIFICATION_SERVICE_ID)
    stopForeground(true)
  }
}
