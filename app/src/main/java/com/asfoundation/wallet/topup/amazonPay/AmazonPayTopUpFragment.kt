package com.asfoundation.wallet.topup.amazonPay

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
import androidx.annotation.Nullable
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.widgets.GenericError
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.amazonPay.AmazonPayIABFragment
import com.asfoundation.wallet.billing.amazonPay.AmazonPayIABFragment.Companion

import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.APP_LINK_HOST
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.APP_LINK_PATH
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConst.Companion.createAmazonTransactionLink
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpFragment.Companion.BONUS
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpFragment.Companion.TOP_UP_AMOUNT
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpFragment.Companion.TOP_UP_CURRENCY
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpFragment.Companion.TOP_UP_CURRENCY_SYMBOL
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AmazonPayTopUpFragment : BasePageViewFragment() {

  private val viewModel: AmazonPayTopUpViewModel by viewModels()

  @Inject
  lateinit var navigator: TopUpNavigator

  @Inject
  lateinit var analytics: TopUpAnalytics

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private lateinit var resultAuthLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    if (arguments?.getSerializable(PAYMENT_DATA) != null) {
      viewModel.paymentData =
        arguments?.getSerializable(PAYMENT_DATA) as TopUpPaymentData
    }
    registerWebViewResult()
    viewModel.getPaymentLink()
    return ComposeView(requireContext()).apply { setContent { MainContent() } }
  }

  @Composable
  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  fun MainContent() {
    Scaffold(
      containerColor = styleguide_blue,
    ) { _ ->
      when (viewModel.uiState.collectAsState().value) {
        is UiState.Success -> {
          handleCompletePurchase()
        }

        UiState.Idle,
        UiState.Loading -> {
          Loading()
        }

        is UiState.Error -> {
          analytics.sendErrorEvent(
            value = viewModel.paymentData.appcValue.toDouble(),
            paymentMethod = "amazon_pay",
            status = "error",
            errorCode = viewModel.amazonTransaction?.errorCode ?: "",
            errorDetails = viewModel.amazonTransaction?.errorContent ?: ""
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
            buttonAnalytics = buttonsAnalytics
          )
        }

        UiState.PaymentRedirect3ds -> {
          viewModel.amazonTransaction?.redirectUrl?.let { redirectUsingUniversalLink(it) }
          Loading()
        }

        UiState.PaymentLinkSuccess -> {
          createAmazonPayLink()
          Loading()
        }
      }
    }
  }

  @Composable
  private fun Loading() {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
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

  override fun onResume() {
    super.onResume()
    viewModel.getAmazonCheckoutSessionId()
  }

  private fun handleCompletePurchase() {
    val bundle = Bundle().apply {
      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
      putString(TOP_UP_AMOUNT, viewModel.paymentData.fiatValue)
      putString(TOP_UP_CURRENCY, viewModel.paymentData.fiatCurrencyCode)
      putString(BONUS, viewModel.paymentData.bonusValue.toString())
      putString(TOP_UP_CURRENCY_SYMBOL, viewModel.paymentData.fiatCurrencySymbol)
    }
    navigator.popView(bundle)
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
      Log.i("Amazon", "redirectUsingUniversalLink: startActivity")
      startActivity(intent)
    } else {
      Log.i("Amazon", "redirectUsingUniversalLink: redirectInCCT")
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

  private fun registerWebViewResult() {
    resultAuthLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data?.dataString?.contains(HostProperties.AMAZON_PAY_REDIRECT_BASE_URL) == true) {
          Log.d(this.tag, "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")
          val uri = Uri.parse(result.data?.dataString)
          val amazonCheckoutSessionId = uri.getQueryParameter("amazonCheckoutSessionId")
          viewModel.getAmazonCheckoutSessionId(amazonCheckoutSessionId)
        } else if (
          result.resultCode == Activity.RESULT_CANCELED
        ) {
          Log.d(this.tag, "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
          navigator.navigateBack()
        }
      }
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
    const val PAYMENT_DATA = "data"
    const val CHROME_PACKAGE_NAME = "com.android.chrome"
  }
}
