package com.asfoundation.wallet.main

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.firebase_messaging.PushNotificationProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PendingIntentNavigator @Inject constructor(@param:ApplicationContext val context: Context) :
  Navigator {

  fun getHomePendingIntent(notificationType: String? = null, requestCode: Int = 0): PendingIntent {
    val taskStackBuilder = NavDeepLinkBuilder(context)
      .setGraph(R.navigation.nav_bar_graph)
      .setDestination(R.id.home_fragment)
      .setComponentName(MainActivity::class.java)
      .createTaskStackBuilder()

    notificationType?.let {
      taskStackBuilder.editIntentAt(0)
        ?.putExtra(PushNotificationProperties.NOTIFICATION_TYPE_KEY, it)
    }

    return taskStackBuilder.getPendingIntent(
      requestCode,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )!!
  }
}
