package com.asfoundation.wallet.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.appcoins.wallet.home.HomeScreen
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment

//TODO to be removed. for testing
class TestHomeFragment : BasePageViewFragment(),
  SingleStateFragment<HomeState, HomeSideEffect> {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        HomeScreen()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onStateChanged(state: HomeState) {
  }

  override fun onSideEffect(sideEffect: HomeSideEffect) {
  }

}
