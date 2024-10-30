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
import com.appcoins.wallet.core.network.microservices.model.emptyPaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.emptyWalletInfo
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.emptyProductInfoData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.APPCPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.PayPalV1PaymentMethod
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PaymentMethodRow
import com.asfoundation.wallet.iab.presentation.PaymentMethodSkeleton
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.PurchaseInfo
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.addClick
import com.asfoundation.wallet.iab.presentation.conditional
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.iab.theme.IAPTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class PaymentMethodsFragment : IabBaseFragment() {

  private val args by navArgs<PaymentMethodsFragmentArgs>()

  private val purchaseInfoData by lazy { args.purchaseInfoDataExtra }

  @Composable
  override fun FragmentContent() = PaymentMethodsContent(
    navigator = navigator,
    purchaseInfoData = purchaseInfoData,
    paymentManager = paymentManager,
  )
}

@Composable
private fun PaymentMethodsContent(
  navigator: FragmentNavigator,
  paymentManager: PaymentManager,
  purchaseInfoData: PurchaseInfoData?,
) {
  val context = LocalContext.current

  val closeIAP: () -> Unit = { context.getActivity()?.finish() }
  val onSupportClick = { navigator.onSupportClick() }
  val onBackClick = { navigator.navigateUp() }

  if (purchaseInfoData != null) {
    val viewModel = rememberPaymentMethodsViewModel(
      paymentManager = paymentManager,
      purchaseInfoData = purchaseInfoData
    )
    val state by viewModel.uiState.collectAsState()

    var isPurchaseInfoExpanded by rememberSaveable { mutableStateOf(false) }
    val onPurchaseInfoExpandClick = { isPurchaseInfoExpanded = !isPurchaseInfoExpanded }

    val onPaymentMethodClick: (PaymentMethod) -> Unit = { paymentMethod ->
      viewModel.setSelectedPaymentMethod(paymentMethod)
      navigator.navigateUp()
    }

    RealPaymentMethodsContent(
      state = state,
      isPurchaseInfoExpanded = isPurchaseInfoExpanded,
      onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
      onSecondaryButtonClick = closeIAP,
      onSupportClick = onSupportClick,
      onBackClick = onBackClick,
      onPaymentMethodClick = onPaymentMethodClick,
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
  onBackClick: () -> Unit,
  onPaymentMethodClick: (PaymentMethod) -> Unit,
) {
  IAPTheme {
    IAPBottomSheet(
      showWalletIcon = false,
      fullscreen = true,
      onBackClick = onBackClick,
    ) {
      when (state) {
        is PaymentMethodsUiState.PaymentMethodsIdle -> PaymentMethodsScreen(
          purchaseInfoData = state.purchaseInfo,
          paymentMethods = state.paymentMethods,
          onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
          isPurchaseInfoExpanded = isPurchaseInfoExpanded,
          onPaymentMethodClick = onPaymentMethodClick,
        )

        is PaymentMethodsUiState.LoadingPaymentMethods -> PaymentMethodsScreen(
          purchaseInfoData = state.purchaseInfo,
          isPurchaseInfoExpanded = isPurchaseInfoExpanded,
          onPurchaseInfoExpandClick = onPurchaseInfoExpandClick,
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
  paymentMethods: List<PaymentMethod>? = null,
  onPurchaseInfoExpandClick: () -> Unit,
  onPaymentMethodClick: ((PaymentMethod) -> Unit)? = null,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    PurchaseInfo(
      modifier = Modifier
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
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      paymentMethods?.forEach {
        PaymentMethodRow(
          modifier = Modifier
            .conditional(
              condition = it.isEnable && onPaymentMethodClick != null,
              ifTrue = { addClick(onClick = { onPaymentMethodClick!!(it) }, "${it.id}Clicked") }
            )
            .padding(24.dp),
          paymentMethodData = it
        )
      } ?: repeat(6) {
        PaymentMethodSkeleton(Modifier.padding(24.dp))
      }
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
    onSecondaryButtonClick = {},
    onBackClick = {},
    onPaymentMethodClick = {},
  )
}

private class PaymentMethodsFragmentUiStateProvider :
  PreviewParameterProvider<PaymentMethodsUiState> {
    
  override val values = sequenceOf(
    PaymentMethodsUiState.PaymentMethodsIdle(
      purchaseInfo = emptyPurchaseInfo,
      paymentMethods = listOf(
        APPCPaymentMethod(
          paymentMethod = emptyPaymentMethodEntity,
          purchaseData = emptyPurchaseData,
          currencyFormatUtils = CurrencyFormatUtils(),
          walletInfo = emptyWalletInfo,
          productInfoData = emptyProductInfoData,
        ),
        CreditCardPaymentMethod(
          paymentMethod = emptyPaymentMethodEntity,
          purchaseData = emptyPurchaseData,
        ),
        PayPalV1PaymentMethod(
          paymentMethod = emptyPaymentMethodEntity,
          purchaseData = emptyPurchaseData,
        ),
      ),
    ),
    PaymentMethodsUiState.LoadingPaymentMethods(
      purchaseInfo = emptyPurchaseInfo
    ),
    PaymentMethodsUiState.PaymentMethodsError,
    PaymentMethodsUiState.NoConnection,
  )
}
