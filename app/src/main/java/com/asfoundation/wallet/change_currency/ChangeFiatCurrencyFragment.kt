package com.asfoundation.wallet.change_currency

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.appcoins.wallet.core.utils.jvm_common.DevUtils.CUSTOM_TAG
import com.appcoins.wallet.feature.changecurrency.ui.ChangeFiatCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeFiatCurrencyFragment : BasePageViewFragment() {

  companion object {
    fun newInstance() = ChangeFiatCurrencyFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            ChangeFiatCurrencyRoute(
              onExitClick = { handleBackPress() }
            )
          }
        }
      }
    }
  }

  private fun handleBackPress() {
    parentFragmentManager.popBackStack()
  }
}