package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavDirections
import androidx.navigation.fragment.navArgs
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.error.IABError
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.emptyCreditCardPaymentMethod
import com.asfoundation.wallet.iab.presentation.AnimatedContentWithoutAnimationOnSameState
import com.asfoundation.wallet.iab.presentation.BonusInfo
import com.asfoundation.wallet.iab.presentation.BonusInfoData
import com.asfoundation.wallet.iab.presentation.BonusInfoSkeleton
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IABLoading
import com.asfoundation.wallet.iab.presentation.IABOpaqueButton
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PaymentMethodRow
import com.asfoundation.wallet.iab.presentation.PaymentMethodSkeleton
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.PurchaseInfo
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoSkeleton
import com.asfoundation.wallet.iab.presentation.addClick
import com.asfoundation.wallet.iab.presentation.conditional
import com.asfoundation.wallet.iab.presentation.emptyBonusInfoData
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.iab.theme.IAPTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@AndroidEntryPoint
class MainFragment : IabBaseFragment() {

  private val args by navArgs<MainFragmentArgs>()

  private val error by lazy { args.parsingError }

  @Composable
  override fun FragmentContent() = MainScreen(
    navigator = navigator,
    paymentManager = paymentManager,
    error = error
  )
}

@Composable
private fun MainScreen(
  navigator: FragmentNavigator,
  paymentManager: PaymentManager,
  error: IABError?
) {
  val context = LocalContext.current

  val closeIAP: () -> Unit = { context.getActivity()?.finish() }
  val onSupportClick = { navigator.onSupportClick() }
  val onNavigateTo: (NavDirections) -> Unit = { directions ->
    navigator.navigateTo(directions = directions)
  }

  IAPTheme {
    if (error == null) {
      val viewModel = rememberMainViewModel(
        paymentManager = paymentManager
      )

      val state by viewModel.uiState.collectAsState()

      val loadData = { viewModel.reload() }

      val onPaymentMethodClick: (PurchaseInfoData) -> Unit = { purchaseInfoData ->
        onNavigateTo(
          MainFragmentDirections.actionNavigateToPaymentMethodsFragment(
            purchaseInfoDataExtra = purchaseInfoData,
          )
        )
      }

      val onBuyClick: (PaymentMethod?, PurchaseInfoData) -> Unit =
        { paymentMethod, purchaseInfoData ->
          when {
            paymentMethod?.isEnable == true -> viewModel.createTransaction(paymentMethod)
            else -> onPaymentMethodClick(purchaseInfoData)
          }
        }

      RealMainScreen(
        state = state,
        onSupportClick = onSupportClick,
        loadData = loadData,
        onPaymentMethodClick = onPaymentMethodClick,
        onBuyClick = onBuyClick,
      )
    } else {
      PurchaseDataError(
        onSupportClick = onSupportClick,
        onSecondaryButtonClick = closeIAP
      )
    }
  }
}

@Composable
private fun PurchaseDataError(
  onSecondaryButtonClick: () -> Unit,
  onSupportClick: () -> Unit,
) {
  IAPBottomSheet(
    showWalletIcon = false,
    fullscreen = false,
  ) {
    GenericError(
      secondaryButtonText = "Close", // TODO hardcoded string
      onSecondaryButtonClick = onSecondaryButtonClick,
      onSupportClick = onSupportClick
    )
  }
}

@Composable
private fun RealMainScreen(
  state: MainFragmentUiState,
  loadData: () -> Unit,
  onSupportClick: () -> Unit,
  onPaymentMethodClick: (PurchaseInfoData) -> Unit,
  onBuyClick: (PaymentMethod?, PurchaseInfoData) -> Unit,
) {
  val showWalletIcon = state is MainFragmentUiState.Idle ||
      state is MainFragmentUiState.LoadingPurchaseData

  IAPBottomSheet(
    showWalletIcon = showWalletIcon,
    fullscreen = false,
  ) {
    AnimatedContentWithoutAnimationOnSameState(targetState = state) { targetState ->
      when (targetState) {
        MainFragmentUiState.LoadingDisclaimer ->
          LoadingDisclaimerScreen()

        is MainFragmentUiState.LoadingPurchaseData ->
          PurchaseDetailsSkeleton(
            showDisclaimer = targetState.showDisclaimer,
            showPreSelectedPaymentMethod = targetState.showPreSelectedPaymentMethod,
          )

        is MainFragmentUiState.Idle -> PurchaseDetails(
          purchaseInfoData = targetState.purchaseInfoData,
          bonusInfoData = targetState.bonusInfoData,
          showDisclaimer = targetState.showDisclaimer,
          preSelectedPaymentMethod = targetState.preSelectedPaymentMethod,
          bonusAvailable = targetState.bonusAvailable,
          onPaymentMethodClick = onPaymentMethodClick,
          onBuyClick = onBuyClick
        )

        MainFragmentUiState.NoConnection -> GenericError(
          secondaryButtonText = "Try Again", // TODO hardcoded string
          onSecondaryButtonClick = loadData,
          onSupportClick = onSupportClick
        )

        MainFragmentUiState.Error -> GenericError(
          secondaryButtonText = "Try Again", // TODO hardcoded string
          onSecondaryButtonClick = loadData,
          onSupportClick = onSupportClick
        )
      }
    }
  }
}

@Composable
private fun LoadingDisclaimerScreen(modifier: Modifier = Modifier) {
  IABLoading(modifier = modifier.fillMaxSize().padding(32.dp))
}

@Composable
private fun PurchaseDetails(
  modifier: Modifier = Modifier,
  purchaseInfoData: PurchaseInfoData,
  bonusInfoData: BonusInfoData,
  showDisclaimer: Boolean,
  bonusAvailable: Boolean,
  preSelectedPaymentMethod: PaymentMethod?,
  onPaymentMethodClick: (PurchaseInfoData) -> Unit,
  onBuyClick: (PaymentMethod?, PurchaseInfoData) -> Unit
) {
  var showLoadingPrice by rememberSaveable { mutableStateOf(false) }
  var paymentMethodId by rememberSaveable { mutableStateOf<String?>(null) }

  if (preSelectedPaymentMethod != null && paymentMethodId != preSelectedPaymentMethod.id) {
    LaunchedEffect(Unit) {
      showLoadingPrice = true
      delay(TimeUnit.SECONDS.toMillis(1))
      paymentMethodId = preSelectedPaymentMethod.id
      showLoadingPrice = false
    }
  }

  var isPurchaseInfoExpanded by rememberSaveable { mutableStateOf(false) }
  var isBonusInfoExpanded by rememberSaveable { mutableStateOf(false) }

  val onPurchaseInfoExpandClick = {
    isPurchaseInfoExpanded = !isPurchaseInfoExpanded
    if (isPurchaseInfoExpanded) isBonusInfoExpanded = false
  }
  val onBonusInfoExpandClick = {
    isBonusInfoExpanded = !isBonusInfoExpanded
    if (isBonusInfoExpanded) isPurchaseInfoExpanded = false
  }

  Column(
    modifier = modifier
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier
        .animateContentSize()
        .padding(bottom = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      PurchaseInfo(
        modifier = Modifier
          .conditional(
            purchaseInfoData.hasFees,
            {
              addClick(
                onClick = onPurchaseInfoExpandClick,
                testTag = "onPurchaseInfoExpandClick"
              )
            }
          )
          .padding(16.dp),
        purchaseInfo = purchaseInfoData,
        isExpanded = isPurchaseInfoExpanded,
        showLoadingPrice = showLoadingPrice
      )
      SeparatorLine()
      BonusInfo(
        modifier = Modifier
          .conditional(
            condition = bonusAvailable,
            ifTrue = {
              addClick(
                onClick = onBonusInfoExpandClick,
                testTag = "onBonusInfoExpandClick"
              )
            }
          )
          .padding(16.dp),
        bonusInfoData = bonusInfoData,
        isExpanded = isBonusInfoExpanded,
        bonusAvailable = bonusAvailable,
        onPromoCodeAvailableClick = { /* TODO on promo code available click */ }
      )
      if (preSelectedPaymentMethod != null) {
        SeparatorLine()
        PaymentMethodRow(
          modifier = Modifier
            .conditional(
              condition = preSelectedPaymentMethod.isEnable,
              ifTrue = {
                addClick(
                  onClick = { onPaymentMethodClick(purchaseInfoData) },
                  testTag = "onPaymentMethodClick"
                )
              }
            )
            .padding(16.dp),
          paymentMethodData = preSelectedPaymentMethod,
          showArrow = true,
        )
      }
    }
    if (showDisclaimer) {
      Text(
        modifier = Modifier
          .padding(horizontal = 6.dp)
          .padding(bottom = 16.dp),
        text = "By adding a payment method you agree and consent to be supplied with the content upon payment and thereby lose my right of withdrawal.", // TODO hardcoded text
        style = IAPTheme.typography.bodySmall,
        color = IAPTheme.colors.smallText,
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    IABOpaqueButton(
      text = when {
        preSelectedPaymentMethod == null -> "Add payment method" // TODO hardcoded text
        preSelectedPaymentMethod.isEnable -> "Buy" // TODO hardcoded text
        else -> "Change payment method" // TODO hardcoded text
      },
      onClick = { onBuyClick(preSelectedPaymentMethod, purchaseInfoData) },
      testTag = when {
        preSelectedPaymentMethod == null -> "addPaymentMethodClicked"
        preSelectedPaymentMethod.isEnable -> "buyClicked"
        else -> "changePaymentMethodClicked"
      }
    )
  }
}

@Composable
private fun PurchaseDetailsSkeleton(
  modifier: Modifier = Modifier,
  showDisclaimer: Boolean,
  showPreSelectedPaymentMethod: Boolean,
) {
  Column(
    modifier = modifier
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier
        .padding(bottom = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      PurchaseInfoSkeleton(modifier = Modifier.padding(16.dp))
      SeparatorLine()
      BonusInfoSkeleton(modifier = Modifier.padding(16.dp))
      if (showPreSelectedPaymentMethod) {
        SeparatorLine()
        PaymentMethodSkeleton(modifier = Modifier.padding(16.dp))
      }
    }
    if (showDisclaimer) {
      Text(
        modifier = Modifier
          .padding(horizontal = 6.dp)
          .padding(bottom = 16.dp),
        text = "By adding a payment method you agree and consent to be supplied with the content upon payment and thereby lose my right of withdrawal.", // TODO hardcoded text
        style = IAPTheme.typography.bodySmall,
        color = IAPTheme.colors.smallText,
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    IABOpaqueButton(
      text = "Add payment method", // TODO hardcoded text
      onClick = { },
      isEnabled = false,
    )
  }
}

@Composable
private fun SeparatorLine(modifier: Modifier = Modifier) {
  Spacer(
    modifier = modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(IAPTheme.colors.primary)
  )
}

@PreviewAll
@Composable
private fun PreviewMainScreen(
  @PreviewParameter(MainFragmentUiStateProvider::class) state: MainFragmentUiState,
) {
  IAPTheme {
    RealMainScreen(
      state = state,
      onSupportClick = {},
      loadData = {},
      onPaymentMethodClick = {},
      onBuyClick = { _, _ -> },
    )
  }
}

private class MainFragmentUiStateProvider : PreviewParameterProvider<MainFragmentUiState> {
  private val bonusAvailable by lazy { Random.nextBoolean() }
  private val showDisclaimer by lazy { Random.nextBoolean() }
  private val showPreSelectedPaymentMethod by lazy { Random.nextBoolean() }

  override val values = sequenceOf(
    MainFragmentUiState.Idle(
      showDisclaimer = showDisclaimer,
      preSelectedPaymentMethod = emptyCreditCardPaymentMethod.takeIf { Random.nextBoolean() },
      bonusAvailable = bonusAvailable,
      purchaseInfoData = emptyPurchaseInfo
        .copy(
          packageName = emptyPurchaseData.domain,
          hasFees = Random.nextBoolean()
        ),
      bonusInfoData = emptyBonusInfoData,
      purchaseData = emptyPurchaseData,
    ),
    MainFragmentUiState.Error,
    MainFragmentUiState.NoConnection,
    MainFragmentUiState.LoadingDisclaimer,
    MainFragmentUiState.LoadingPurchaseData(
      showDisclaimer = showDisclaimer,
      showPreSelectedPaymentMethod = showPreSelectedPaymentMethod,
    ),
  )
}
