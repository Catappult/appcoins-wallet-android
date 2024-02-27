package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.core.arch.data.Error
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.ScreenTitle
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.component.Animation
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationInfoModel
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.NavigateToPaymentUrl
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState.PaymentCompleted
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
          ScreenTitle(stringResource(R.string.paypal_verification_header))
          PayPalVerificationContent(padding = padding)
        }
  }

  @Composable
  fun PayPalVerificationContent(padding: PaddingValues) {
    Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
      when (val uiState = viewModel.uiState.collectAsState().value) {
        PaymentCompleted -> {
          CodeInputScreen()
        }
        VerificationPaypalState.Success -> {
          SuccessScreen()
        }
        is VerificationPaypalState.Error,
        UnknownError, -> {
          Text(stringResource(id = R.string.unknown_error)) // TODO show error
        }
        is NavigateToPaymentUrl -> {
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
        else -> CircularProgressIndicator()
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
          modifier = Modifier.padding(top = 16.dp),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Medium)
      ButtonWithText(
          label = stringResource(id = R.string.continue_button),
          onClick = onVerificationClick,
          labelColor = WalletColors.styleguide_white,
          backgroundColor = WalletColors.styleguide_pink,
          buttonType = ButtonType.LARGE)
    }
  }

  @Composable
  fun CodeInputScreen() {
    var defaultCode by rememberSaveable { mutableStateOf("") }

    Column {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.transact_loading_animation)
        Text(
            text = stringResource(id = R.string.paypal_verification_home_one_step_card_title),
            color = WalletColors.styleguide_light_grey,
            modifier = Modifier.padding(top = 28.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(id = R.string.paypal_verification_insert_code_body),
            color = WalletColors.styleguide_light_grey,
            modifier = Modifier.padding(top = 16.dp, bottom = 28.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium)

        WalletTextField(
            defaultCode,
            "",
            backgroundColor = WalletColors.styleguide_blue_secondary,
            keyboardType = KeyboardType.Number,
            onValueChange = { newCode -> defaultCode = newCode })
        Text(
            text = stringResource(id = R.string.paypal_verification_didnt_receive_title),
            color = WalletColors.styleguide_dark_grey,
            modifier = Modifier.padding(top = 48.dp),
        )
        TextButton(onClick = {}) {
          Text(stringResource(id = R.string.resend_button), color = WalletColors.styleguide_pink)
        }
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        ButtonWithText(
            label = stringResource(id = R.string.cancel_button),
            onClick = { navigator.navigateBack() },
            labelColor = WalletColors.styleguide_white,
            outlineColor = WalletColors.styleguide_white)
        Spacer(modifier = Modifier.width(20.dp))
        ButtonWithText(
            label = stringResource(id = R.string.send_button),
            onClick = { viewModel.verifyCode(defaultCode) },
            labelColor = WalletColors.styleguide_white,
            backgroundColor = WalletColors.styleguide_pink)
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
          modifier = Modifier.padding(top = 16.dp),
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Medium)
      ButtonWithText(
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
    InitialScreen(amount = "€20", onVerificationClick = {})
  }

  @Preview
  @Composable
  fun PreviewCodeInputScreen() {
    CodeInputScreen()
  }

  @Preview
  @Composable
  fun PreviewSuccessScreen() {
    SuccessScreen()
  }

  private fun getPaypalData() =
      VerificationPaypalData(RedirectComponent.getReturnUrl(requireContext()))

  private fun getFormattedAmount(verificationInfoModel: VerificationInfoModel): String {
    return verificationInfoModel.symbol +
        formatter.formatCurrency(verificationInfoModel.value, WalletCurrency.FIAT)
  }
}
