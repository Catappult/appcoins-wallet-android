package com.asfoundation.wallet.manage_wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.TopBar
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageWalletFragment : BasePageViewFragment() {
  private val viewModel: ManageWalletViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { ManageWalletView() } }
  }

  @Composable
  @OptIn(ExperimentalMaterial3Api::class)
  fun ManageWalletView() {
    Scaffold(
      topBar = {
        Surface(shadowElevation = 4.dp) {
          TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
        }
      }, containerColor = styleguide_blue
    ) { padding ->
      ManageWalletContent(padding = padding)
    }
  }

  @Composable
  internal fun ManageWalletContent(padding: PaddingValues) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      DummyCard()
    }
  }

  @Composable
  fun DummyCard() {
    Card(
      modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        .fillMaxWidth()
        .height(200.dp),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(styleguide_blue_secondary),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Manage Wallet Screen",
          style = MaterialTheme.typography.titleMedium,
          color = styleguide_white
        )
      }
    }
  }
}
