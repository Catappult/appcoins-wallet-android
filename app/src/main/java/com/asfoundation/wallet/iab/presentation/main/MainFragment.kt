package com.asfoundation.wallet.iab.presentation.main

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PaymentMethod
import com.asfoundation.wallet.iab.presentation.PaymentMethodSkeleton
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.emptyPaymentMethodData
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
    RealMainScreen(
      showWalletIcon = showWalletIcon,
    )
  }
}

@Composable
private fun RealMainScreen(
  modifier: Modifier = Modifier,
  showWalletIcon: Boolean
) {
  IAPBottomSheet(
    modifier = modifier,
    showWalletIcon = showWalletIcon,
    fullscreen = false,
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      PaymentMethodSkeleton(modifier = Modifier.padding(16.dp))
      SeparatorLine()
      PaymentMethod(
        modifier = Modifier.padding(16.dp),
        paymentMethodData = emptyPaymentMethodData,
        paymentMethodEnabled = true,
        showArrow = true
      )
      SeparatorLine()
      PaymentMethod(
        modifier = Modifier.padding(16.dp),
        paymentMethodData = emptyPaymentMethodData,
        paymentMethodEnabled = true,
        showArrow = false
      )
      SeparatorLine()
      PaymentMethod(
        modifier = Modifier.padding(16.dp),
        paymentMethodData = emptyPaymentMethodData.copy(paymentMethodDescription = null),
        paymentMethodEnabled = false,
        showArrow = true
      )
      SeparatorLine()
      PaymentMethod(
        modifier = Modifier.padding(16.dp),
        paymentMethodData = emptyPaymentMethodData,
        paymentMethodEnabled = false,
        showArrow = false
      )
      SeparatorLine()
      PaymentMethod(
        modifier = Modifier.padding(16.dp),
        paymentMethodData = emptyPaymentMethodData,
        paymentMethodEnabled = false,
        showArrow = true
      )
    }
  }
}

@Composable
private fun SeparatorLine(modifier: Modifier = Modifier) {
  Spacer(
    modifier = modifier
      .height(1.dp)
      .fillMaxWidth()
      .background(IAPTheme.colors.primary)
  )
}

@PreviewAll
@Composable
private fun PreviewMainScreen() {
  val showWalletIcon = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

  IAPTheme {
    RealMainScreen(showWalletIcon = showWalletIcon)
  }
}
