package com.asfoundation.wallet.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.App
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics

abstract class BasePageViewFragment : Fragment() {

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