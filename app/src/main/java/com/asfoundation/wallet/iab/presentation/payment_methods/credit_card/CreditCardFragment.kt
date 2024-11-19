package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardView
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.ExpiryDateInput
import com.adyen.checkout.card.ui.SecurityCodeInput
import com.adyen.checkout.components.base.BaseActionComponent
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.utils.android_common.extensions.getActivity
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.payment_manager.payment_methods.emptyCreditCardPaymentMethod
import com.asfoundation.wallet.iab.presentation.AnimatedContentWithoutAnimationOnSameState
import com.asfoundation.wallet.iab.presentation.GenericError
import com.asfoundation.wallet.iab.presentation.IABLoading
import com.asfoundation.wallet.iab.presentation.IABOpaqueButton
import com.asfoundation.wallet.iab.presentation.IAPBottomSheet
import com.asfoundation.wallet.iab.presentation.PaymentMethodRow
import com.asfoundation.wallet.iab.presentation.PreviewAll
import com.asfoundation.wallet.iab.presentation.PurchaseInfo
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.emptyPurchaseInfo
import com.asfoundation.wallet.iab.presentation.isInPortrait
import com.asfoundation.wallet.iab.theme.IAPTheme
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import kotlin.random.Random

@AndroidEntryPoint
class CreditCardFragment : IabBaseFragment() {

  companion object {
    private const val EXTRA_PAYMENT_METHOD_ID = "${BuildConfig.APPLICATION_ID}.PAYMENT_METHOD_ID"
    fun createBundleArgs(paymentMethodId: String) =
      bundleOf(EXTRA_PAYMENT_METHOD_ID to paymentMethodId)
  }

  private val paymentMethodId by lazy { arguments?.getString(EXTRA_PAYMENT_METHOD_ID) ?: "" }

  @Composable
  override fun FragmentContent() = CreditCardContent(
    navigator = navigator,
    paymentManager = paymentManager,
    paymentMethodId = paymentMethodId
  )
}

@Composable
private fun CreditCardContent(
  navigator: FragmentNavigator,
  paymentManager: PaymentManager,
  paymentMethodId: String
) {
  val activityResultRegistry =
    LocalActivityResultRegistryOwner.current!!.activityResultRegistry
  val context = LocalContext.current.getActivity() as ComponentActivity
  val viewModel = rememberCreditCardViewModel(
    activity = context,
    paymentManager = paymentManager,
    paymentMethodId = paymentMethodId
  )
  val uiState by viewModel.uiState.collectAsState()
  val uiStateSavingCard by viewModel.uiStateSavingCard.collectAsState()
  val uiStateInputError by viewModel.uiStateInputError.collectAsState()

  var adyenCardWrapper by rememberSaveable { mutableStateOf<AdyenCardWrapper?>(null) }

  val onUserAction: (BaseActionComponent<*>, PaymentModel) -> Unit = { _, paymentModel ->
    navigator.navigateToUriForResult(
      activityResultRegistry,
      AdyenResultContract(paymentModel),
      viewModel::onWebViewResult,
      paymentModel.redirectUrl ?: ""
    )
  }
  val onReloadClick = {
    viewModel.clearAdyenComponents(context)
    viewModel.loadCreditCard()
  }
  val onVerifyClick = { navigator.startActivity(VerificationCreditCardActivity.newIntent(context)) }
  val onSaveCardClick = { viewModel.saveCreditCardData(adyenCardWrapper) }
  val onFinishWithSuccess = { navigator.navigateTo(CreditCardFragmentDirections.actionNavigateToMainFragment()) }
  val onCardComponentStateUpdate: (CardComponentState, PaymentInfoModel) -> Unit =
    { state, paymentInfoModel ->
      state.data.paymentMethod?.let { paymentMethod ->
        val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
        adyenCardWrapper = AdyenCardWrapper(
          cardPaymentMethod = paymentMethod,
          shouldStoreCard = true,
          hasCvc = hasCvc,
          supportedShopperInteractions = paymentInfoModel.supportedShopperInteractions
        )
      }
    }

  IAPTheme {
    CreditCardScreen(
      uiState = uiState,
      uiStateSavingCard = uiStateSavingCard,
      uiStateInputError = uiStateInputError,
      loadData = onReloadClick,
      onBackClick = navigator::navigateUp,
      onSupportClick = navigator::onSupportClick,
      onSaveCardClick = onSaveCardClick,
      onFinishWithSuccess = onFinishWithSuccess,
      submitActionResult = viewModel::submitActionResult,
      onVerifyClick = onVerifyClick,
      onVerifyTryAgainClick = navigator::navigateUp,
      onCardComponentStateUpdate = onCardComponentStateUpdate,
      onUserAction = onUserAction,
    )
  }
}

@Composable
private fun CreditCardScreen(
  uiState: CreditCardUiState,
  uiStateSavingCard: Boolean,
  uiStateInputError: String?,
  loadData: () -> Unit,
  onBackClick: () -> Unit,
  onSupportClick: () -> Unit,
  onSaveCardClick: () -> Unit,
  onFinishWithSuccess: () -> Unit,
  onVerifyClick: () -> Unit,
  onVerifyTryAgainClick: () -> Unit,
  submitActionResult: (String?, JSONObject?, PaymentModel) -> Unit,
  onUserAction: (BaseActionComponent<*>, PaymentModel) -> Unit,
  onCardComponentStateUpdate: (CardComponentState, PaymentInfoModel) -> Unit,
) {
  val context = LocalContext.current.getActivity() as ComponentActivity
  val lifecycleOwner = LocalLifecycleOwner.current
  var isButtonEnabled by rememberSaveable { mutableStateOf(false) }

  IAPBottomSheet(
    fullscreen = true,
    onBackClick = onBackClick,
  ) {
    AnimatedContentWithoutAnimationOnSameState(targetState = uiState) { targetState ->
      when (targetState) {
        CreditCardUiState.Loading ->
          IABLoading(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp)
          )

        is CreditCardUiState.Idle -> CreditCardIdle(
          purchaseInfo = targetState.purchaseInfoData,
          creditCardPaymentMethod = targetState.creditCardPaymentMethod,
          onSaveCardClick = onSaveCardClick,
          showLoading = uiStateSavingCard,
          isButtonEnabled = isButtonEnabled,
        ) {
          LaunchedEffect(Unit) {
            targetState.cardComponent?.invoke(context)?.let { cardComponent ->
              cardComponent.observe(lifecycleOwner) { state ->
                isButtonEnabled = state.isInputValid
                onCardComponentStateUpdate(state, targetState.paymentInfoModel)
              }
            }
          }

          targetState.cardComponent?.let { cardComponent ->
            AdyenCreditCardView(
              cardComponent = cardComponent(context)!!,
              isEnabled = !uiStateSavingCard,
              errorOnCVC = uiStateInputError,
            )
          }
        }

        is CreditCardUiState.AddCreditCardError -> GenericError(
          secondaryButtonText = "Try Again", // TODO hardcoded string
          onSecondaryButtonClick = loadData,
          onSupportClick = onSupportClick,
          messageText = targetState.errorMessage
        )

        CreditCardUiState.CreditCardError -> GenericError(
          secondaryButtonText = "Try Again", // TODO hardcoded string
          onSecondaryButtonClick = loadData,
          onSupportClick = onSupportClick
        )

        CreditCardUiState.NoConnection -> GenericError(
          secondaryButtonText = "Try Again", // TODO hardcoded string
          onSecondaryButtonClick = loadData,
          onSupportClick = onSupportClick
        )

        is CreditCardUiState.VerifyScreen -> GenericError(
          titleText = targetState.message,
          primaryButtonText = stringResource(id = com.asf.wallet.R.string.referral_view_verify_button),
          onPrimaryButtonClick = onVerifyClick,
          secondaryButtonText = "Try with another method", // TODO hardcoded text
          onSecondaryButtonClick = onVerifyTryAgainClick,
          onSupportClick = onSupportClick
        )

        is CreditCardUiState.UserAction<*> ->
          LaunchedEffect(targetState.paymentModel.uid) {
            onUserAction(
              targetState.component,
              targetState.paymentModel
            )
          }

        is CreditCardUiState.Handle3DSAction -> {
          LaunchedEffect(Unit) {
            targetState.component.observe(lifecycleOwner) {
              submitActionResult(it.paymentData, it.details, targetState.paymentModel)
              targetState.component.removeObservers(lifecycleOwner)
            }
            targetState.component.handleAction(context, targetState.paymentModel.action!!)
          }
        }

        is CreditCardUiState.HandleRedirectAction -> {
          LaunchedEffect(Unit) {
            targetState.component.observe(lifecycleOwner) {
              submitActionResult(it.paymentData, it.details, targetState.paymentModel)
              targetState.component.removeObservers(lifecycleOwner)
            }
            targetState.component.handleIntent(Intent("", targetState.uri))
          }
        }

        CreditCardUiState.Finish ->
          LaunchedEffect(Unit) { onFinishWithSuccess() }
      }
    }
  }
}

@Composable
private fun CreditCardIdle(
  modifier: Modifier = Modifier,
  purchaseInfo: PurchaseInfoData,
  creditCardPaymentMethod: CreditCardPaymentMethod,
  showLoading: Boolean,
  isButtonEnabled: Boolean,
  onSaveCardClick: () -> Unit,
  adyenContent: @Composable (ColumnScope.() -> Unit),
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    if (isInPortrait()) {
      PurchaseInfo(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(top = 16.dp),
        purchaseInfo = purchaseInfo
      )
    }
    Column(
      modifier = Modifier
        .animateContentSize()
        .padding(16.dp)
        .fillMaxHeight()
        .weight(1F)
        .clip(RoundedCornerShape(12.dp))
        .background(IAPTheme.colors.primaryContainer)
    ) {
      PaymentMethodRow(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(top = 16.dp),
        paymentMethodData = creditCardPaymentMethod,
        showDescription = false,
      )
      adyenContent()
      AnimatedVisibility(showLoading, enter = fadeIn(), exit = fadeOut()) {
        IABLoading(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f),
          animationSize = 64.dp,
          text = "Adding Card...", // TODO hardcoded text
        )
      }
      AnimatedVisibility(!showLoading, enter = fadeIn(), exit = fadeOut()) {
        Column {
          Spacer(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
          )
          IABOpaqueButton(
            modifier = Modifier.padding(16.dp),
            text = "Add card",
            onClick = onSaveCardClick,
            isEnabled = isButtonEnabled,
            testTag = "AddCardClick"
          )
        }
      }
    }
  }
}

@Composable
private fun AdyenCreditCardView(
  modifier: Modifier = Modifier,
  isEnabled: Boolean,
  cardComponent: CardComponent,
  errorOnCVC: String?
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  AndroidView(
    factory = {
      CardView(it).apply {
        attach(cardComponent, lifecycleOwner)
      }
    },
    update = {
      it.findViewById<TextInputLayout>(com.asf.wallet.R.id.textInputLayout_securityCode).error =
        errorOnCVC
      it.findViewById<TextInputLayout>(R.id.textInputLayout_cardNumber).isEnabled = isEnabled
      it.findViewById<SecurityCodeInput>(R.id.editText_securityCode).isEnabled = isEnabled
      it.findViewById<ExpiryDateInput>(R.id.editText_expiryDate).isEnabled = isEnabled
      it.findViewById<SwitchCompat>(R.id.switch_storePaymentMethod)?.run {
        isVisible = false
        isChecked = true
      }
    },
    modifier = modifier.fillMaxWidth(),
  )
}

@PreviewAll
@Composable
private fun CreditCardIdlePreview(
  @PreviewParameter(CreditCardIdleLoadingUiStateProvider::class) isLoading: Boolean,
) {
  IAPTheme {
    IAPBottomSheet(
      modifier = Modifier
        .safeDrawingPadding()
        .background(Color.Gray),
      fullscreen = true
    ) {
      CreditCardIdle(
        purchaseInfo = emptyPurchaseInfo,
        creditCardPaymentMethod = emptyCreditCardPaymentMethod,
        showLoading = isLoading,
        onSaveCardClick = { },
        adyenContent = {
          Text(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .padding(top = 16.dp)
              .fillMaxWidth()
              .height(100.dp)
              .wrapContentHeight(),
            text = "AdyenView",
            textAlign = TextAlign.Center
          )
        },
        isButtonEnabled = Random.nextBoolean(),
      )
    }
  }
}

@PreviewAll
@Composable
private fun CreditCardScreenPreview(
  @PreviewParameter(CreditCardFragmentUiStateProvider::class) uiState: CreditCardUiState,
) {
  IAPTheme {
    CreditCardScreen(
      uiState = uiState,
      uiStateSavingCard = false,
      uiStateInputError = null,
      loadData = { },
      onBackClick = { },
      onSaveCardClick = { },
      onSupportClick = { },
      onFinishWithSuccess = { },
      onCardComponentStateUpdate = { _, _ -> },
      onUserAction = { _, _ -> },
      onVerifyTryAgainClick = { },
      onVerifyClick = { },
      submitActionResult = { _, _, _ -> },
    )
  }
}

private class CreditCardIdleLoadingUiStateProvider :
  PreviewParameterProvider<Boolean> {

  override val values = sequenceOf(
    true,
    false
  )
}

private class CreditCardFragmentUiStateProvider :
  PreviewParameterProvider<CreditCardUiState> {

  override val values = sequenceOf(
    CreditCardUiState.Loading,
    CreditCardUiState.AddCreditCardError("Some error message"),
    CreditCardUiState.CreditCardError,
    CreditCardUiState.NoConnection,
  )
}
