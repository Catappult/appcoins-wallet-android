package com.asfoundation.wallet.promotions.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asf.wallet.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromotionBroadcastReceiver @Inject constructor() : BroadcastReceiver() {

  companion object {
    const val EXTRA_VIP_REFERRAL_CODE = "${BuildConfig.APPLICATION_ID}.VIP_REFERRAL_CODE"

    fun createPendingIntent(
      context: Context?,
      vipReferralCode: String,
    ) = Intent(context, PromotionBroadcastReceiver::class.java).apply {
      putExtra(EXTRA_VIP_REFERRAL_CODE, vipReferralCode)
    }
  }

  @Inject
  lateinit var promotionNotification: PromotionNotification

  override fun onReceive(context: Context?, intent: Intent?) {
    intent?.getStringExtra(EXTRA_VIP_REFERRAL_CODE)
      ?.let { promotionNotification.sendPromotionNotification(it) }
  }
}
