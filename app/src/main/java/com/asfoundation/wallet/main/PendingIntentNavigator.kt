package com.asfoundation.wallet.main

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PendingIntentNavigator @Inject constructor(
  @ApplicationContext val context: Context
) : Navigator {

  fun getHomePendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
      .setGraph(R.navigation.home_graph)
      .setDestination(R.id.home_fragment)
      .setComponentName(MainActivity::class.java)
      .createPendingIntent()
  }

  fun getPromotionsPendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
      .setGraph(R.navigation.promotions_graph)
      .setDestination(R.id.promotions_fragment)
      .setComponentName(MainActivity::class.java)
      .createPendingIntent()
  }

  fun getMyWalletsPendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
      .setGraph(R.navigation.my_wallets_graph)
      .setDestination(R.id.my_wallets_fragment)
      .setComponentName(MainActivity::class.java)
      .createPendingIntent()
  }
}