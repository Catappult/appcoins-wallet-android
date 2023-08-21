package com.wallet.appcoins.core.legacy_base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.legacy.PageViewAnalytics
import javax.inject.Inject

abstract class BasePageViewDialogFragment : DialogFragment() {

  private lateinit var pageViewAnalytics: PageViewAnalytics

  @Inject lateinit var analyticsManager: AnalyticsManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    pageViewAnalytics = PageViewAnalytics(analyticsManager)
  }

  override fun onResume() {
    super.onResume()
    pageViewAnalytics.sendPageViewEvent(javaClass.simpleName)
  }
}