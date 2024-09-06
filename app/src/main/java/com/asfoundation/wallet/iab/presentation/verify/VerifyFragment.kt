package com.asfoundation.wallet.iab.presentation.verify

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.theme.IAPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyFragment : IabBaseFragment() {

  @Composable
  override fun FragmentContent() = RealVerifyScreen(findNavController())
}

@Composable
fun RealVerifyScreen(navController: NavController) {
  val context = LocalContext.current
  val viewModel = rememberVerifyViewModel(navController = navController)

  val onPrimaryButtonClick = {
    viewModel.navigateToVerify(context)
  }

  val onSecondaryButtonClick = {
    viewModel.popBackStack()
  }

  val onSupportClick = {
    viewModel.launchChat()
  }

  VerifyScreen(
    onPrimaryButtonClick = onPrimaryButtonClick,
    onSecondaryButtonClick = onSecondaryButtonClick,
    onSupportClick = onSupportClick,
  )
}

@Composable
private fun VerifyScreen(
  onPrimaryButtonClick: (() -> Unit)? = null,
  onSecondaryButtonClick: (() -> Unit),
  onSupportClick: () -> Unit,
) {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = false
    ) {
      GenericError(
        titleText = stringResource(id = R.string.onboarding_verification_body),
        primaryButtonText = stringResource(id = R.string.referral_view_verify_button),
        onPrimaryButtonClick = onPrimaryButtonClick,
        secondaryButtonText = "Try with another method",// TODO review copy. does not exist at the moment
        onSecondaryButtonClick = onSecondaryButtonClick,
        onSupportClick = onSupportClick
      )
    }
  }
}

@PreviewAll
@Composable
fun PreviewVerifyScreen() {
  VerifyScreen(
    onPrimaryButtonClick = {},
    onSecondaryButtonClick = {},
    onSupportClick = {},
  )
}
