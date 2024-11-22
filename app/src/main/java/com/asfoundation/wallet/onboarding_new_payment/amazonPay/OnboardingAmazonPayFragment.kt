package com.asfoundation.wallet.onboarding_new_payment.amazonPay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.GenericError
import com.appcoins.wallet.ui.widgets.component.Animation
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.billing.amazonPay.AmazonPayIABFragment
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.APP_LINK_HOST
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.APP_LINK_PATH

import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.createAmazonTransactionLink
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingAmazonPayFragment : BasePageViewFragment() {

  private val viewModel: OnboardingAmazonPayViewModel by viewModels()
  private val args by navArgs<OnboardingAmazonPayFragmentArgs>()

  @Inject
  lateinit var navigator: OnboardingAmazonPayNavigator

  @Inject
  lateinit var analytics: OnboardingPaymentEvents

  @Inject
  lateinit var formatter: CurrencyFormatUtils


  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private var resultAuthLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.data?.dataString?.contains(HostProperties.AMAZON_PAY_REDIRECT_BASE_URL) == true) {
        val uri = Uri.parse(result.data?.dataString)
        val amazonCheckoutSessionId = uri.getQueryParameter("amazonCheckoutSessionId")
        viewModel.getAmazonCheckoutSessionId(amazonCheckoutSessionId)
      } else if (
        result.resultCode == Activity.RESULT_CANCELED
      ) {
        navigator.navigateBack()
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    viewModel.getPaymentLink()
    viewModel.sendPaymentStartEvent(args.transactionBuilder)
    return ComposeView(requireContext()).apply { setContent { MainContent() } }
  }

  @Composable
  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  fun MainContent() {
    Scaffold(
      containerColor = styleguide_blue_secondary,
    ) { _ ->
      when (viewModel.uiState.collectAsState().value) {
        is UiState.Success -> {
          handleCompletePurchase()
          TransactionCompletedScreen()
        }

        UiState.Idle,
        UiState.Loading -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
          }
        }

        is UiState.Error -> {
          analytics.sendPaymentErrorMessageEvent(
            errorMessage = "AmazonPay transaction error.",
            transactionBuilder = args.transactionBuilder,
            paymentMethod = BillingAnalytics.PAYMENT_METHOD_AMAZON_PAY,
          )
          GenericError(
            message = stringResource(R.string.activity_iab_error_message),
            onSupportClick = {
              viewModel.launchChat()
            },
            onTryAgain = {
              navigator.navigateBack()
            },
            fragmentName = fragmentName,
            buttonAnalytics = buttonsAnalytics,
            isDarkTheme = false
          )
        }

        UiState.PaymentRedirect3ds -> {
          viewModel.amazonTransaction?.redirectUrl?.let {
//            redirectUsingUniversalLink(it)
            startWebViewAuthorization(it)
          }
        }

        UiState.PaymentLinkSuccess -> {
          createAmazonPayLink()
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
          }
        }
      }
    }
  }

  @Composable
  fun TransactionCompletedScreen() {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Animation(
        modifier = Modifier
          .size(100.dp)
          .padding(top = 16.dp),
        animationRes = R.raw.success_animation,
        iterations = 1,
        restartOnPlay = false
      )
      Text(
        text = stringResource(R.string.transaction_status_success),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = styleguide_white,
        modifier = Modifier.padding(top = 10.dp)
      )

      Row(
        modifier = Modifier
          .padding(top = 6.dp),
        verticalAlignment = CenterVertically
      ) {
        Animation(
          modifier = Modifier.size(28.dp),
          animationRes = R.raw.bonus_gift_animation
        )
        Text(
          text = stringResource(
            R.string.purchase_success_bonus_received_title,
            args.forecastBonus.getPurchaseBonusMessage(formatter)
          ),
          fontSize = 12.sp,
          color = styleguide_dark_grey,
          modifier = Modifier.padding(start = 8.dp)
        )
      }
      Spacer(modifier = Modifier.weight(1f))

      ButtonWithText(
        label = stringResource(R.string.back_to_the_game_button),
        onClick = {
          analytics.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.BACK_TO_THE_GAME)
          navigator.navigateBackToGame(args.transactionBuilder.domain)
        },
        backgroundColor = styleguide_pink,
        labelColor = styleguide_white,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      )
      ButtonWithText(
        label = stringResource(R.string.onboarding_1st_explore_wallet_button),
        onClick = {
          analytics.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.EXPLORE_WALLET)
          navigator.navigateToHome()
        },
        outlineColor = styleguide_white,
        labelColor = styleguide_white,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 16.dp)
      )
    }
  }

  private fun createAmazonPayLink() {
    val params = createAmazonTransactionLink(
      merchantId = viewModel.amazonTransaction?.merchantId,
      payload = viewModel.amazonTransaction?.payload,
      checkoutSessionId = viewModel.amazonTransaction?.checkoutSessionId
    )
    buildURL(params, "ES")
  }

  private fun handleCompletePurchase() {
    viewModel.amazonTransaction?.uid?.let {
      viewModel.sendPaymentSuccessEvent(
        args.transactionBuilder,
        it
      )
    }
  }


  private fun buildURL(parameters: Map<String, String?>, region: String) {
    val uriBuilder = Uri.Builder()
    for ((key, value) in parameters) {
      uriBuilder.appendQueryParameter(key, value)
    }
    uriBuilder.scheme("https")
      .authority(APP_LINK_HOST.getValue(region))
      .path(APP_LINK_PATH.getValue(region))

    Log.d("AMAZON", "buildURL: ${uriBuilder.build().toString()}")
    startWebViewAuthorization(uriBuilder.build().toString())
  }

  private fun redirectUsingUniversalLink(url: String) {
    viewModel.runningCustomTab = true
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val amazonPackageName = "com.amazon.mShop.android.shopping"
    // Check if the Amazon app is installed
    val isAmazonAppInstalled = try {
      requireContext().packageManager.getPackageInfo(
        amazonPackageName,
        PackageManager.GET_ACTIVITIES
      )
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
    if (isAmazonAppInstalled) {
      startActivity(intent)
    } else {
      redirectInCCT(url)
    }
  }

  private fun redirectInCCT(url: String) {
    viewModel.runningCustomTab = true
    val customTabsBuilder = CustomTabsIntent.Builder().build()
    customTabsBuilder.intent.setPackage(AmazonPayIABFragment.CHROME_PACKAGE_NAME)
    customTabsBuilder.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    customTabsBuilder.launchUrl(requireContext(), Uri.parse(url))
  }

  private fun startWebViewAuthorization(url: String) {
    viewModel.runningCustomTab = true
    val intent = WebViewActivity.newIntent(requireActivity(), url)
    resultAuthLauncher.launch(intent)
  }

  @Preview
  @Composable
  fun PreviewLoading() {
    MainContent()
  }


  companion object {
    const val CHROME_PACKAGE_NAME = "com.android.chrome"
  }
}
