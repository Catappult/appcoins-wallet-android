package com.asfoundation.wallet.iab.presentation.main

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.fragment.navArgs
import com.asf.wallet.R
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PreviewAll
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
  var state by rememberSaveable { mutableStateOf(1) }

  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false,
    ) {
      GenericError(
        titleText = "Showing state $state of 4",
        messageText = stringResource(id = R.string.unknown_error).takeIf { state < 2 || state == 3 },
        primaryButtonText = "Verify Payment Method".takeIf { state < 3 },
        onPrimaryButtonClick = {},
        secondaryButtonText = "Change to next state",
        onSecondaryButtonClick = { if (state == 4) state = 1 else state++ },
        onSupportClick = {},
      )
    }
  }
}

@PreviewAll
@Composable
fun PreviewMainScreen() {
  IAPTheme {
    GenericError(
      messageText = stringResource(id = R.string.unknown_error),
      primaryButtonText = "Verify Payment Method",
      onPrimaryButtonClick = {},
      secondaryButtonText = "Try again",
      onSecondaryButtonClick = {},
      onSupportClick = {},
    )
  }
}
