package com.asfoundation.wallet.iab.presentation.main

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.fragment.navArgs
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.SuccessScreen
import com.asfoundation.wallet.iab.theme.IAPTheme

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

  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false,
    ) {
      SuccessScreen(bonus = "€0.05")
    }
  }
}

@PreviewAll
@Composable
fun PreviewMainScreen() {
  IAPTheme {
    SuccessScreen(bonus = "€0.05")
  }
}
