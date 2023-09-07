package com.asfoundation.wallet.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.appcoins.wallet.feature.invoices.ui.InvoicesRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InvoicesFragment : BasePageViewFragment() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  companion object {
    fun newInstance() = InvoicesFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            InvoicesRoute(
              onChatClick = { displayChat() })
          }
        }
      }
    }
  }
}