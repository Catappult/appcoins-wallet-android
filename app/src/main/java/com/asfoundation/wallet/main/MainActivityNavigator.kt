package com.asfoundation.wallet.main

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.navigation.NavDeepLinkBuilder
import com.asf.wallet.R
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.ui.overlay.OverlayFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MainActivityNavigator @Inject constructor(@ApplicationContext val context: Context) {

  fun getHomePendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setGraph(R.navigation.home_graph)
        .setDestination(R.id.home_fragment)
        .setComponentName(MainActivity::class.java)
        .createPendingIntent()
  }

  fun navigateToHome() {
    val ctxt = context
    if (ctxt is MainActivity) {
      ctxt.setSelectedBottomNavItem(MainActivity.BottomNavItem.HOME)
    } else {
      getHomePendingIntent().send()
    }
  }

  fun getPromotionsPendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setGraph(R.navigation.promotions_graph)
        .setDestination(R.id.promotions_fragment)
        .setComponentName(MainActivity::class.java)
        .createPendingIntent()
  }

  fun navigateToPromotions() {
    val ctxt = context
    if (ctxt is MainActivity) {
      ctxt.setSelectedBottomNavItem(MainActivity.BottomNavItem.PROMOTIONS)
    } else {
      getPromotionsPendingIntent().send()
    }
  }

  fun getMyWalletsPendingIntent(): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setGraph(R.navigation.my_wallets_graph)
        .setDestination(R.id.my_wallets_fragment)
        .setComponentName(MainActivity::class.java)
        .createPendingIntent()
  }

  fun navigateToMyWallets() {
    val ctxt = context
    if (ctxt is MainActivity) {
      ctxt.setSelectedBottomNavItem(MainActivity.BottomNavItem.MY_WALLETS)
    } else {
      getMyWalletsPendingIntent().send()
    }
  }

  fun navigateToTopUp() {
    val intent = TopUpActivity.newIntent(context)
        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
    context.startActivity(intent)
  }

  fun showPromotionsOverlay(index: Int) {
    if (context is MainActivity) {
      context.supportFragmentManager.beginTransaction()
          .setCustomAnimations(R.anim.fragment_fade_in_animation,
              R.anim.fragment_fade_out_animation, R.anim.fragment_fade_in_animation,
              R.anim.fragment_fade_out_animation)
          .add(R.id.tooltip_container,
              OverlayFragment.newInstance(index))
          .addToBackStack(OverlayFragment::class.java.name)
          .commit()
    }
  }
}