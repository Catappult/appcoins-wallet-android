package com.asfoundation.wallet.main

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PendingIntentNavigator @Inject constructor(@ApplicationContext val context: Context) :
    Navigator {

  fun getHomePendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setGraph(R.navigation.nav_bar_graph)
        .setDestination(R.id.home_fragment)
        .setComponentName(MainActivity::class.java)
        .createPendingIntent()
  }
}
