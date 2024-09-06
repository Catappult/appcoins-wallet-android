package com.asfoundation.wallet.iab.presentation.main

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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
  override fun FragmentContent() = MainScreen(findNavController(), purchaseData)
}

@Composable
private fun MainScreen(navController: NavController, purchaseData: PurchaseData?) {
  var showWalletIcon by remember { mutableStateOf(false) }
  showWalletIcon = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

  val viewModel = rememberMainViewModel(navController)

  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false,
    ) {
      GenericError(
        titleText = "Screen to navigate to Verify",
        secondaryButtonText = "Navigate to verify",
        onSecondaryButtonClick = { viewModel.navigateTo(MainFragmentDirections.actionNavigateToVerifyFragment()) },
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
