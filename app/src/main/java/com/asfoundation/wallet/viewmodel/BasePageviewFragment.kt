package com.asfoundation.wallet.viewmodel

import android.os.Bundle
import com.asfoundation.wallet.App
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics
import dagger.android.support.DaggerFragment

abstract class BasePageViewFragment : DaggerFragment() {

  private lateinit var pageViewAnalytics: PageViewAnalytics

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    pageViewAnalytics = PageViewAnalytics((activity?.application as App).analyticsManager())
  }

  override fun onResume() {
    super.onResume()

    pageViewAnalytics.sendPageViewEvent(javaClass.simpleName)
  }
}