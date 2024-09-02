package com.asfoundation.wallet.iab.presentation.main

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.fragment.navArgs
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PreviewAll

class MainFragment : IabBaseFragment() {

  private val args by navArgs<MainFragmentArgs>()

  private val purchaseData by lazy { args.purchaseDataExtra }

  @Composable
  override fun FragmentContent() = MainScreen(purchaseData)
}

@Composable
private fun MainScreen(purchaseData: PurchaseData?) {
  var showWalletIcon by remember { mutableStateOf(false) }
  showWalletIcon = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

  IAPBottomSheet(
    showWalletIcon = showWalletIcon,
    fullscreen = false,
  ) {
    Text(
      modifier = Modifier.background(Color.White),
      text = "Hello"
    )
  }
}

@PreviewAll
@Composable
fun PreviewMainScreen() {
  WalletTheme {
    MainScreen(purchaseData = emptyPurchaseData)
  }
}