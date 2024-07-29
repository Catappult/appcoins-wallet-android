package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.GenericError
import com.appcoins.wallet.ui.widgets.ScreenTitle
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.component.Animation
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletCodeTextField
import com.appcoins.wallet.ui.widgets.expanded
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.VerificationAnalytics
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationInfoModel
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.Idle
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.Loading
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.OpenWebPayPalPaymentRequest
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.RequestVerificationCode
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.ShowVerificationInfo
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.UnknownError
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
        WebViewActivity.SUCCESS -> viewModel.successPayment()
        WebViewActivity.FAIL, WebViewActivity.USER_CANCEL -> viewModel.failPayment()
      }
    }

  companion object {
    const val CONTINUE = "continue"
    const val SEND = "send"
    const val CANCEL = "cancel"
    const val RESEND = "resend"
    const val GOT_IT = "got_it"
    const val TRY_AGAIN = "try_again"
    const val APPCOINS_SUPPORT = "appcoins_support"
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

  @Composable
  private fun PayPalVerificationScreen() {
    Scaffold(
      topBar = { TopBar(onClickSupport = { viewModel.launchChat() }) },
      containerColor = WalletColors.styleguide_blue
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
        is RequestVerificationCode -> {
          CodeInputScreen(
            uiState.wrongCode,
            uiState.loading,
            onVerificationClick =
            {
              viewModel.launchVerificationPayment(getPaypalData())
            }
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
            buttonAnalytics = buttonsAnalytics)
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
        backgroundColor = WalletColors.styleguide_pink,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Composable
  fun CodeInputScreen(wrongCode: Boolean, loading: Boolean, onVerificationClick: () -> Unit = {}) {
    var defaultCode by rememberSaveable { mutableStateOf("") }
    BoxWithConstraints {
      if (expanded())
        CodeInputScreenLandscape(
          wrongCode = wrongCode,
          loading = loading,
          onVerificationClick = onVerificationClick,
          onCodeChange = { newCode -> defaultCode = newCode },
          code = defaultCode
        )
      else
        CodeInputScreenPortrait(
          wrongCode = wrongCode,
          loading = loading,
          onVerificationClick = onVerificationClick,
          onCodeChange = { newCode -> defaultCode = newCode },
          code = defaultCode
        )

    }
  }

  @Composable
  fun CodeInputScreenPortrait(
    wrongCode: Boolean,
    loading: Boolean,
    onVerificationClick: () -> Unit = {},
    onCodeChange: (String) -> Unit,
    code: String
  ) {
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
        text = stringResource(id = R.string.paypal_verification_insert_code_body),
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier
          .padding(top = 16.dp, bottom = 28.dp)
          .padding(horizontal = 16.dp)
          .widthIn(max = 332.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
      )

      InputCodeTextField(
        wrongCode = wrongCode,
        defaultCode = onCodeChange,
        code = code
      )

      ResendCode(
        Modifier.padding(top = 48.dp), isVisible = !loading, onVerificationClick
      )
      Spacer(modifier = Modifier.weight(72f))
      SendCodeButtons(Modifier.padding(top = 40.dp), loading = loading, code = code)
    }
  }

  @Composable
  fun CodeInputScreenLandscape(
    wrongCode: Boolean, loading: Boolean, onVerificationClick: () -> Unit = {},
    onCodeChange: (String) -> Unit,
    code: String,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Animation(modifier = Modifier.size(128.dp), animationRes = R.raw.verify_animation)
        Column(modifier = Modifier.padding(start = 24.dp)) {
          Text(
            text = stringResource(id = R.string.paypal_verification_home_one_step_card_title),
            color = WalletColors.styleguide_light_grey,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = stringResource(id = R.string.paypal_verification_insert_code_body),
            color = WalletColors.styleguide_light_grey,
            modifier = Modifier
              .padding(top = 8.dp, bottom = 32.dp)
              .widthIn(max = 332.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Medium
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            InputCodeTextField(wrongCode = wrongCode, defaultCode = onCodeChange, code)
            ResendCode(
              modifier = Modifier.padding(start = 24.dp),
              isVisible = !loading,
              onVerificationClick = onVerificationClick
            )
          }
        }
      }
      Spacer(Modifier.height(40.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        SendCodeButtons(loading = loading, code = code)
      }
    }
  }

  @Composable
  fun InputCodeTextField(wrongCode: Boolean, defaultCode: (String) -> Unit, code: String) {
    WalletCodeTextField(
      wrongCode = wrongCode, onValueChange = { newCode -> defaultCode(newCode) }, code = code
    )
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
        Text(stringResource(id = R.string.start_again_button), color = WalletColors.styleguide_pink)
      }

    }
  }

  @Composable
  fun SendCodeButtons(modifier: Modifier = Modifier, loading: Boolean, code: String) {
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier.widthIn(max = 312.dp)
    ) {
      if (loading)
        Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
      else {
        ButtonWithText(
          modifier = Modifier.weight(1f),
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
        Spacer(modifier = Modifier.width(20.dp))
        ButtonWithText(
          modifier = Modifier.weight(1f),
          label = stringResource(id = R.string.send_button),
          onClick = {
            viewModel.verifyCode(code)
            analytics.sendInsertCodeScreenEvent(action = SEND)
          },
          labelColor = WalletColors.styleguide_white,
          backgroundColor = WalletColors.styleguide_pink,
          buttonType = ButtonType.LARGE,
          enabled = code.hasFourDigits(),
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
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
        backgroundColor = WalletColors.styleguide_pink,
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
  fun PreviewCodeInputScreen() {
    CodeInputScreen(
      wrongCode = true, loading = false
    )
  }

  @Preview(widthDp = 610)
  @Composable
  fun PreviewCodeInputScreenLandscape() {
    CodeInputScreen(
      wrongCode = true, loading = false
    )
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

  private fun String.hasFourDigits() = length == 4
}
