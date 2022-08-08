package com.asfoundation.wallet.main.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashExtenderFragment : Fragment(),
  SingleStateFragment<ViewState, SideEffect> {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.splash_extender_fragment, container, false)
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}

