package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.core.arch.data.Error
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
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationInfoModel
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.ResendCodeStatus
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.OpenWebPayPalPaymentRequest
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.RequestVerificationCode
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.ShowVerificationInfo
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.UnknownError
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerificationPaypalFragment : BasePageViewFragment() {

  @Inject lateinit var navigator: VerificationPaypalNavigator

  @Inject lateinit var formatter: CurrencyFormatUtils

  private val viewModel: VerificationPaypalViewModel by viewModels()

  private val paypalActivityLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
          result: ActivityResult ->
        when (result.resultCode) {
          WebViewActivity.SUCCESS -> viewModel.successPayment()
          WebViewActivity.FAIL -> viewModel.failPayment()
          WebViewActivity.USER_CANCEL -> viewModel.cancelPayment()
        }
      }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { PayPalVerificationScreen() } }
  }

  private fun setError(error: Error) {
    if (error is Error.ApiError.NetworkError) showNetworkError()
    else if (error.throwable.message.equals(WebViewActivity.USER_CANCEL_THROWABLE))
        handleUserCancelError()
    else showGenericError()
  }

  private fun showNetworkError() {}

  private fun showGenericError() {}

  private fun handleUserCancelError() {
    navigator.navigateBack()
  }

  @Composable
  private fun PayPalVerificationScreen() {
    Scaffold(
        topBar = { TopBar(onClickSupport = { viewModel.launchChat() }) },
        containerColor = WalletColors.styleguide_blue) { padding ->
          Column(
              modifier =
                  Modifier.padding(padding)
                      .verticalScroll(rememberScrollState())
                      .height(IntrinsicSize.Max)) {
                ScreenTitle(stringResource(R.string.paypal_verification_header))
                PayPalVerificationContent()
              }
        }
  }

  @Composable
  fun PayPalVerificationContent() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is RequestVerificationCode -> {
          CodeInputScreen(uiState.wrongCode, uiState.loading, uiState.resendCodeStatus)
        }
        VerificationPaypalState.VerificationCompleted -> {
          SuccessScreen()
        }
        is VerificationPaypalState.Error,
        UnknownError -> {
          GenericError(
              message = stringResource(R.string.manage_cards_error_details),
              onSupportClick = { viewModel.launchChat() },
              onTryAgain = { viewModel.fetchVerificationStatus() })
        }
        is OpenWebPayPalPaymentRequest -> {
          navigator.navigateToPayment(uiState.url, paypalActivityLauncher)
        }
        is ShowVerificationInfo -> {
          InitialScreen(
              amount = getFormattedAmount(uiState.verificationInfo.verificationInfoModel),
              onVerificationClick = {
                viewModel.launchVerificationPayment(
                    getPaypalData(), uiState.verificationInfo.paymentInfoModel.paymentMethod)
              })
        }
        else -> FullScreenLoading()
      }
    }
  }

  @Composable
  fun InitialScreen(amount: String, onVerificationClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Image(
          painter = painterResource(id = R.drawable.ic_paypal_circle),
          contentDescription = null,
          modifier = Modifier.size(112.dp))
      Text(
          text = stringResource(R.string.verification_verify_paypal_description, amount),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(top = 24.dp).padding(horizontal = 16.dp),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Medium)
      ButtonWithText(
          modifier = Modifier.padding(top = 40.dp),
          label = stringResource(id = R.string.continue_button),
          onClick = onVerificationClick,
          labelColor = WalletColors.styleguide_white,
          backgroundColor = WalletColors.styleguide_pink,
          buttonType = ButtonType.LARGE)
    }
  }

  @Composable
  fun CodeInputScreen(wrongCode: Boolean, loading: Boolean, resendCodeStatus: ResendCodeStatus) {
    var defaultCode by rememberSaveable { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.verify_animation)
      Text(
          text = stringResource(id = R.string.paypal_verification_home_one_step_card_title),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(top = 28.dp),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold)
      Text(
          text = stringResource(id = R.string.paypal_verification_insert_code_body),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(top = 16.dp, bottom = 28.dp).padding(horizontal = 16.dp),
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Medium)

      WalletCodeTextField(
          wrongCode = wrongCode, onValueChange = { newCode -> defaultCode = newCode })

      ResendCode(
          Modifier.padding(top = 48.dp), isVisible = !loading, resendCodeStatus = resendCodeStatus)

      SendCodeButtons(Modifier.padding(top = 40.dp), loading = loading, code = defaultCode)
    }
  }

  @Composable
  fun ResendCode(
      modifier: Modifier = Modifier,
      isVisible: Boolean,
      resendCodeStatus: ResendCodeStatus
  ) {
    val alphaVisibility = if (isVisible) 1f else 0f
    val timer = "00:58"
    Column(
        modifier = modifier.alpha(alphaVisibility),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = stringResource(id = R.string.paypal_verification_didnt_receive_title),
              color = WalletColors.styleguide_dark_grey,
              fontWeight = FontWeight.Medium)

          when (resendCodeStatus) {
            ResendCodeStatus.AvailableToResend -> {
              ResendButton()
            }
            ResendCodeStatus.Resending -> {
              Animation(modifier = modifier.size(40.dp), animationRes = R.raw.loading_wallet)
            }
            ResendCodeStatus.Resent -> {
              CodeSent()
            }
            ResendCodeStatus.UnavailableToResend -> {
              Text(
                  text = "Resend ($timer)",
                  color = WalletColors.styleguide_dark_grey,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(top = 16.dp))
            }
          }
        }
  }

  @Composable
  fun ResendButton() {
    TextButton(onClick = { viewModel.resendCode() }) {
      Text(stringResource(id = R.string.resend_button), color = WalletColors.styleguide_pink)
    }
  }

  @Composable
  fun CodeSent() {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
          Image(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              colorFilter = ColorFilter.tint(WalletColors.styleguide_green))
          Text(
              text = "Code sent",
              color = WalletColors.styleguide_light_grey,
              fontWeight = FontWeight.Medium)
        }
  }

  @Composable
  fun SendCodeButtons(modifier: Modifier = Modifier, loading: Boolean, code: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier) {
          if (loading)
              Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
          else {
            ButtonWithText(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.cancel_button),
                onClick = { navigator.navigateBack() },
                labelColor = WalletColors.styleguide_white,
                outlineColor = WalletColors.styleguide_white,
                buttonType = ButtonType.LARGE)
            Spacer(modifier = Modifier.width(20.dp))
            ButtonWithText(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.send_button),
                onClick = { viewModel.verifyCode(code) },
                labelColor = WalletColors.styleguide_white,
                backgroundColor = WalletColors.styleguide_pink,
                buttonType = ButtonType.LARGE)
          }
        }
  }

  @Composable
  fun SuccessScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.success_animation)
      Text(
          text = stringResource(id = R.string.activity_iab_transaction_completed_title),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(top = 28.dp),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold)
      Text(
          text = stringResource(id = R.string.paypal_verification_completed_body),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp),
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Medium)
      ButtonWithText(
          modifier = Modifier.padding(top = 40.dp),
          label = stringResource(id = R.string.got_it_button),
          onClick = { navigator.navigateBack() },
          labelColor = WalletColors.styleguide_white,
          backgroundColor = WalletColors.styleguide_pink,
          buttonType = ButtonType.LARGE)
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
        wrongCode = true, loading = false, resendCodeStatus = ResendCodeStatus.UnavailableToResend)
  }

  @Preview
  @Composable
  fun PreviewSuccessScreen() {
    SuccessScreen()
  }

  @Preview
  @Composable
  fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
    }
  }

  private fun getPaypalData() =
      VerificationPaypalData(RedirectComponent.getReturnUrl(requireContext()))

  private fun getFormattedAmount(verificationInfoModel: VerificationInfoModel): String {
    return verificationInfoModel.symbol +
        formatter.formatCurrency(verificationInfoModel.value, WalletCurrency.FIAT)
  }
}
