package com.asfoundation.wallet.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.BalanceCard
import com.appcoins.wallet.ui.widgets.TopBar
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import java.math.BigDecimal
import java.util.*

//TODO to be removed. for testing
class TestHomeFragment : BasePageViewFragment(),
  SingleStateFragment<HomeState, HomeSideEffect> {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = WalletColors.styleguide_blue
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
          ) {
            TopBar(
              isMainBar = true,
              isVip = false,
              onClickNotifications = { Log.Companion.d("TestHomeFragment", "Notifications") },
              onClickSettings = { Log.Companion.d("TestHomeFragment", "Settings") },
              onClickSupport = { Log.Companion.d("TestHomeFragment", "Support") },
            )

            BalanceCard(
              balance = BigDecimal(30.12),
              currency = Currency.getAvailableCurrencies().first { it.displayName.equals("Euro") },
              menuItems = listOf(),
              onClickTransfer = { Log.Companion.d("TestHomeFragment", "Transfer") },
              onClickBackup = { Log.Companion.d("TestHomeFragment", "Backup") },
              onClickTopUp = { Log.Companion.d("TestHomeFragment", "Top Up") }
            )
          }
        }
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
