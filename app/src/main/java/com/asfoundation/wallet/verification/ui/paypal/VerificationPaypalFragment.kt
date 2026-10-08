package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.GenericError
import com.appcoins.wallet.ui.widgets.top_bar.ScreenTitle
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.appcoins.wallet.ui.widgets.component.Animation
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.ui.WebViewResults
import com.asfoundation.wallet.verification.ui.credit_card.VerificationAnalytics
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationInfoModel
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.Idle
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.Loading
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.OpenWebPayPalPaymentRequest
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.CheckEmail
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.ShowVerificationInfo
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.UnknownError
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificationPaypalFragment : BasePageViewFragment() {

  @Inject
  lateinit var navigator: VerificationPaypalNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val viewModel: VerificationPaypalViewModel by viewModels()

  @Inject
  lateinit var analytics: VerificationAnalytics

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private val paypalActivityLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      when (result.resultCode) {
        WebViewResults.SUCCESS.code -> viewModel.successPayment()
        WebViewResults.FAIL.code, WebViewResults.USER_CANCEL.code -> viewModel.failPayment()
      }
    }

  companion object {
    const val CONTINUE = "continue"
    const val CANCEL = "cancel"
    const val RESEND = "resend"
    const val GOT_IT = "got_it"
    const val TRY_AGAIN = "try_again"
    const val APPCOINS_SUPPORT = "appcoins_support"
    private const val EMAIL_VERIFICATION_POLL_MS = 5_000L
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent { PayPalVerificationScreen() }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // The user verifies by opening the emailed link (often on another device): check on every
    // resume and keep polling while the "check your email" screen is visible.
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        while (true) {
          viewModel.refreshEmailVerification()
          delay(EMAIL_VERIFICATION_POLL_MS)
        }
      }
    }
  }

  @Composable
  private fun PayPalVerificationScreen() {
    Scaffold(
      topBar = {
        TopBar(
          onClickSupport = { viewModel.launchChat() },
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      },
      containerColor = WalletColors.styleguide_dark
    ) { padding ->
      Column(modifier = Modifier.padding(padding)) {
        ScreenTitle(stringResource(R.string.paypal_verification_header))
        PayPalVerificationContent()
      }
    }
  }

  @Composable
  fun PayPalVerificationContent() {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      when (val uiState = viewModel.uiState.collectAsState().value) {
        CheckEmail -> {
          CheckEmailScreen(
            onStartAgainClick = { viewModel.launchVerificationPayment(getPaypalData()) }
          )
        }

        VerificationPaypalState.VerificationCompleted -> {
          SuccessScreen()
        }

        is VerificationPaypalState.Error,
        UnknownError -> {
          GenericError(
            message = stringResource(R.string.manage_cards_error_details),
            onSupportClick = {
              analytics.sendErrorScreenEvent(action = APPCOINS_SUPPORT)
              viewModel.launchChat()
            },
            onTryAgain = {
              analytics.sendErrorScreenEvent(action = TRY_AGAIN)
              viewModel.fetchVerificationStatus()
            },
            fragmentName = fragmentName,
            buttonAnalytics = buttonsAnalytics
          )
        }

        is OpenWebPayPalPaymentRequest -> {
          navigator.navigateToPayment(uiState.url, paypalActivityLauncher)
        }

        is ShowVerificationInfo -> {
          InitialScreen(
            amount = getFormattedAmount(uiState.verificationInfo.verificationInfoModel),
            onVerificationClick = {
              analytics.sendInitialScreenEvent(action = CONTINUE)
              viewModel.launchVerificationPayment(
                getPaypalData()
              )
            })
        }

        Loading, Idle -> FullScreenLoading()
      }
    }
  }

  @Composable
  fun InitialScreen(amount: String, onVerificationClick: () -> Unit = {}) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
    ) {
      Spacer(modifier = Modifier.weight(112f))
      Image(
        painter = painterResource(id = R.drawable.ic_paypal_circle),
        contentDescription = null,
        modifier = Modifier
          .size(112.dp)
      )
      Text(
        text = stringResource(R.string.verification_verify_paypal_description, amount),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier
          .widthIn(max = 464.dp)
          .padding(top = 24.dp)
          .padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.weight(272f))
      ButtonWithText(
        modifier = Modifier
          .widthIn(max = 360.dp)
          .padding(top = 40.dp),
        label = stringResource(id = R.string.continue_button),
        onClick = onVerificationClick,
        labelColor = WalletColors.styleguide_white,
        backgroundColor = WalletColors.styleguide_primary,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Composable
  fun CheckEmailScreen(onStartAgainClick: () -> Unit = {}) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
    ) {
      Spacer(modifier = Modifier.weight(112f))
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.verify_animation)
      Text(
        text = stringResource(id = R.string.paypal_verification_home_one_step_card_title),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier.padding(top = 28.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = stringResource(id = R.string.paypal_verification_check_email_body),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier
          .padding(top = 16.dp)
          .padding(horizontal = 16.dp)
          .widthIn(max = 332.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
      )
      ResendCode(Modifier.padding(top = 48.dp), isVisible = true, onStartAgainClick)
      Spacer(modifier = Modifier.weight(72f))
      ButtonWithText(
        modifier = Modifier
          .padding(top = 40.dp)
          .widthIn(max = 360.dp),
        label = stringResource(id = R.string.cancel_button),
        onClick = {
          navigator.navigateBack()
          analytics.sendInsertCodeScreenEvent(action = CANCEL)
        },
        labelColor = WalletColors.styleguide_white,
        outlineColor = WalletColors.styleguide_white,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Composable
  fun ResendCode(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onVerificationClick: () -> Unit = {}
  ) {
    val alphaVisibility = if (isVisible) 1f else 0f
    Column(
      modifier = modifier.alpha(alphaVisibility),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(id = R.string.paypal_verification_didnt_receive_title),
        color = WalletColors.styleguide_dark_grey,
        fontWeight = FontWeight.Medium
      )
      TextButton(onClick = {
        analytics.sendInsertCodeScreenEvent(action = RESEND)
        onVerificationClick()
      }) {
        Text(
          stringResource(id = R.string.start_again_button),
          color = WalletColors.styleguide_primary
        )
      }

    }
  }

  @Composable
  fun SuccessScreen() {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
    ) {
      Spacer(modifier = Modifier.weight(72f))
      Animation(
        modifier = Modifier.size(104.dp),
        animationRes = R.raw.success_animation,
        iterations = 1
      )
      Text(
        text = stringResource(id = R.string.activity_iab_transaction_completed_title),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier.padding(top = 28.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = stringResource(id = R.string.paypal_verification_completed_body),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier
          .padding(top = 16.dp)
          .padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.weight(304f))
      ButtonWithText(
        modifier = Modifier
          .padding(top = 40.dp)
          .widthIn(max = 360.dp),
        label = stringResource(id = R.string.got_it_button),
        onClick = {
          analytics.sendSuccessScreenEvent(action = GOT_IT)
          navigator.navigateBack()
        },
        labelColor = WalletColors.styleguide_white,
        backgroundColor = WalletColors.styleguide_primary,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Preview
  @Composable
  fun PreviewInitialScreen() {
    InitialScreen(amount = "€0.50", onVerificationClick = {})
  }

  @Preview
  @Composable
  fun PreviewCheckEmailScreen() {
    CheckEmailScreen()
  }

  @Preview
  @Composable
  fun PreviewSuccessScreen() {
    SuccessScreen()
  }

  @Preview
  @Composable
  fun FullScreenLoading() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(Modifier.weight(1f))
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
      Spacer(Modifier.weight(1f))
    }
  }

  private fun getPaypalData() =
    VerificationPaypalData(RedirectComponent.getReturnUrl(requireContext()))

  private fun getFormattedAmount(verificationInfoModel: VerificationInfoModel): String {
    return verificationInfoModel.symbol +
        formatter.formatCurrency(verificationInfoModel.value, WalletCurrency.FIAT)
  }
}
