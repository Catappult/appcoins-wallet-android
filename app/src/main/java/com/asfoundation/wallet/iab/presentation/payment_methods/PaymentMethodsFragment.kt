package com.asfoundation.wallet.iab.presentation.payment_methods

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.navArgs
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PaymentMethodData
import com.asfoundation.wallet.iab.presentation.PaymentMethodRow
import com.asfoundation.wallet.iab.presentation.PaymentMethodSkeleton
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.PurchaseInfo
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.addClick
import com.asfoundation.wallet.iab.presentation.conditional
import com.asfoundation.wallet.iab.presentation.emptyPaymentMethodData
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

class PaymentMethodsFragment : IabBaseFragment() {

  private val args by navArgs<PaymentMethodsFragmentArgs>()

  private val purchaseData by lazy { args.purchaseDataExtra }
  private val purchaseInfoData by lazy { args.purchaseInfoDataExtra }

  @Composable
  override fun FragmentContent() = PaymentMethodsContent(navigator, purchaseData, purchaseInfoData)
}

@Composable
private fun PaymentMethodsContent(
  navigator: FragmentNavigator,
  purchaseData: PurchaseData?,
  purchaseInfoData: PurchaseInfoData?
) {
  val context = LocalContext.current

  val closeIAP: () -> Unit = { context.getActivity()?.finish() }
  val onSupportClick = { navigator.onSupportClick() }

  if (purchaseData != null && purchaseInfoData != null) {
    val viewModel = rememberPaymentMethodsViewModel(purchaseData, purchaseInfoData)
    val state by viewModel.uiState.collectAsState()

    var isPurchaseInfoExpanded by rememberSaveable { mutableStateOf(false) }
    val onPurchaseInfoExpandClick = { isPurchaseInfoExpanded = !isPurchaseInfoExpanded }

    RealPaymentMethodsContent(
      state = state,
      isPurchaseInfoExpanded = isPurchaseInfoExpanded,
      onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
      onSecondaryButtonClick = closeIAP,
      onSupportClick = onSupportClick,
    )
  } else {
    PurchaseDataError(
      onSecondaryButtonClick = closeIAP,
      onSupportClick = onSupportClick
    )
  }
}

@Composable
private fun RealPaymentMethodsContent(
  state: PaymentMethodsUiState,
  isPurchaseInfoExpanded: Boolean,
  onPurchaseInfoExpandClick: () -> Unit,
  onSecondaryButtonClick: () -> Unit,
  onSupportClick: () -> Unit,
) {
  IAPTheme {
    IAPBottomSheet(showWalletIcon = false, fullscreen = true) {
      when (state) {
        is PaymentMethodsUiState.PaymentMethodsIdle -> PaymentMethodsScreen(
          purchaseInfoData = state.purchaseInfo,
          paymentMethods = state.paymentMethods,
          onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
          isPurchaseInfoExpanded = isPurchaseInfoExpanded,
        )

        is PaymentMethodsUiState.LoadingPaymentMethods -> PaymentMethodsScreen(
          purchaseInfoData = state.purchaseInfo,
          onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
          isPurchaseInfoExpanded = isPurchaseInfoExpanded,
        )

        is PaymentMethodsUiState.PaymentMethodsError -> GenericError(
          secondaryButtonText = "Close", // TODO hardcoded string
          onSecondaryButtonClick = onSecondaryButtonClick,
          onSupportClick = onSupportClick
        )

        is PaymentMethodsUiState.NoConnection -> GenericError(
          secondaryButtonText = "Close", // TODO hardcoded string
          onSecondaryButtonClick = onSecondaryButtonClick,
          onSupportClick = onSupportClick
        )
      }
    }
  }
}

@Composable
fun PaymentMethodsScreen(
  purchaseInfoData: PurchaseInfoData,
  isPurchaseInfoExpanded: Boolean,
  paymentMethods: List<PaymentMethodData>? = null,
  onPurchaseInfoExpandClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
  ) {
    PurchaseInfo(
      modifier = Modifier
        .padding(top = 16.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
        .conditional(
          condition = purchaseInfoData.hasFees,
          ifTrue = {
            addClick(
              onClick = onPurchaseInfoExpandClick,
              testTag = "onPurchaseInfoExpandClick"
            )
          }
        )
        .padding(16.dp),
      purchaseInfo = purchaseInfoData,
      isExpanded = isPurchaseInfoExpanded,
    )

    Column(
      modifier = Modifier
        .padding(top = 16.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      paymentMethods?.forEach {
        PaymentMethodRow(
          modifier = Modifier.padding(16.dp),
          paymentMethodData = it
        )
      } ?: repeat(5) { PaymentMethodSkeleton(Modifier.padding(16.dp)) }
    }
  }
}

@Composable
private fun PurchaseDataError(
  onSecondaryButtonClick: () -> Unit,
  onSupportClick: () -> Unit,
) {
  GenericError(
    secondaryButtonText = "Close", // TODO hardcoded string
    onSecondaryButtonClick = onSecondaryButtonClick,
    onSupportClick = onSupportClick
  )
}

@PreviewAll
@Composable
private fun PaymentMethodsPreview(
  @PreviewParameter(PaymentMethodsFragmentUiStateProvider::class) state: PaymentMethodsUiState
) {
  RealPaymentMethodsContent(
    state = state,
    isPurchaseInfoExpanded = Random.nextBoolean(),
    onPurchaseInfoExpandClick = {},
    onSupportClick = {},
    onSecondaryButtonClick = {}
  )
}

private class PaymentMethodsFragmentUiStateProvider :
  PreviewParameterProvider<PaymentMethodsUiState> {

  override val values = sequenceOf(
    PaymentMethodsUiState.PaymentMethodsIdle(
      paymentMethods = listOf(
        emptyPaymentMethodData.copy(
          id = "1",
          paymentMethodName = "Credit Card",
          isEnable = Random.nextBoolean(),
          paymentMethodDescription = "Message"
        ),
        emptyPaymentMethodData.copy(
          id = "2",
          paymentMethodName = "Credit Card",
          isEnable = Random.nextBoolean(),
          paymentMethodDescription = "Message"
        ),
        emptyPaymentMethodData.copy(
          id = "3",
          paymentMethodName = "Credit Card",
          isEnable = Random.nextBoolean(),
          paymentMethodDescription = "Message"
        ),
      ),
      purchaseInfo = emptyPurchaseInfo
    ),
    PaymentMethodsUiState.LoadingPaymentMethods(
      purchaseInfo = emptyPurchaseInfo
    ),
    PaymentMethodsUiState.PaymentMethodsError,
    PaymentMethodsUiState.NoConnection,
  )
}
