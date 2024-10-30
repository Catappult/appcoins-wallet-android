package com.asfoundation.wallet.billing.amazonPay

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white_75
import com.appcoins.wallet.ui.widgets.GenericError
import com.appcoins.wallet.ui.widgets.component.Animation

import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConsts.Companion.APP_LINK_HOST
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConsts.Companion.APP_LINK_PATH
import com.asfoundation.wallet.billing.amazonPay.models.AmazonConsts.Companion.CHECKOUT_LANGUAGE
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.Navigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class AmazonPayIABFragment : BasePageViewFragment() {

  private val viewModel: AmazonPayTopUpViewModel by viewModels()

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private lateinit var iabView: IabView
  private var navigatorIAB: Navigator? = null


  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    viewModel.getPaymentLink(
      transactionBuilder,
      amount.toString(),
      currency,
      origin
    )
    viewModel.sendPaymentStartEvent(transactionBuilder)
    return ComposeView(requireContext()).apply { setContent { MainContent() } }
  }

  @Composable
  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  fun MainContent() {
    Scaffold(
      Modifier.height(400.dp),
      containerColor = styleguide_white_75,
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
          //send error event
          GenericError(
            message = stringResource(R.string.activity_iab_error_message),
            onSupportClick = {
              viewModel.launchChat()
            },
            onTryAgain = {
              iabView.navigateBack()
            },
            fragmentName = fragmentName,
            buttonAnalytics = buttonsAnalytics,
            isDarkTheme = false
          )
        }

        UiState.PaymentRedirect3ds,
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
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      Animation(
        modifier = Modifier.size(100.dp),
        animationRes = R.raw.transaction_complete_bonus_animation_new
      )
      Text(
        text = stringResource(R.string.transaction_status_success),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .padding(top = 108.dp)
          .align(Alignment.Center)
      )

      Row(
        modifier = Modifier
          .padding(top = 148.dp)
          .align(Alignment.Center),
        verticalAlignment = CenterVertically
      ) {
        Animation(modifier = Modifier.size(28.dp), animationRes = R.raw.bonus_gift_animation)
        Text(
          text = stringResource(R.string.purchase_success_bonus_received_title, bonus),
          fontSize = 12.sp,
          color = styleguide_dark_grey
        )
      }
    }
  }

  private fun createAmazonPayLink() {

    val params = if (BuildConfig.DEBUG) {
      mapOf(
        "merchantId" to viewModel.amazonTransaction?.merchantId,
        "ledgerCurrency" to "EUR",
        "checkoutLanguage" to CHECKOUT_LANGUAGE.getValue("UK"),
        "productType" to "PayOnly",
        "amazonCheckoutSessionId" to viewModel.amazonTransaction?.checkoutSessionId,
        "integrationType" to "NativeMobile",
        "environment" to "SANDBOX",
        "payloadJSON" to viewModel.amazonTransaction?.payload
      )
    } else {
      mapOf(
        "merchantId" to viewModel.amazonTransaction?.merchantId,
        "ledgerCurrency" to "EUR",
        "checkoutLanguage" to CHECKOUT_LANGUAGE.getValue("UK"),
        "productType" to "PayOnly",
        "amazonCheckoutSessionId" to viewModel.amazonTransaction?.checkoutSessionId,
        "integrationType" to "NativeMobile",
        "payloadJSON" to viewModel.amazonTransaction?.payload
      )
    }
    buildURL(params, "ES")
  }

  override fun onResume() {
    super.onResume()
    viewModel.getAmazonCheckoutSessionId()
  }

  private fun handleCompletePurchase() {
    viewModel.amazonTransaction?.uid?.let {
      viewModel.sendPaymentSuccessEvent(
        transactionBuilder,
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
    redirectUsingUniversalLink(uriBuilder.build().toString())
  }

  private fun redirectUsingUniversalLink(url: String) {
    viewModel.runningCustomTab = true
    val customTabsBuilder = CustomTabsIntent.Builder().build()
    customTabsBuilder.intent.setPackage(CHROME_PACKAGE_NAME)
    customTabsBuilder.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    customTabsBuilder.launchUrl(requireContext(), Uri.parse(url))
  }

  @Preview
  @Composable
  fun PreviewLoading() {
    MainContent()
  }

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY_KEY)) {
      requireArguments().getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val origin: String? by lazy {
    if (requireArguments().containsKey(ORIGIN_KEY)) {
      requireArguments().getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_DATA_KEY)) {
      requireArguments().getParcelable<TransactionBuilder>(TRANSACTION_DATA_KEY)!!
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL, 0)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }


  companion object {
    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val IS_SKILLS = "is_skills"
    private const val FREQUENCY = "frequency"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"
    const val CHROME_PACKAGE_NAME = "com.android.chrome"

    @JvmStatic
    fun newInstance(
      paymentType: PaymentType,
      origin: String?,
      transactionBuilder: TransactionBuilder,
      amount: BigDecimal,
      currency: String?,
      bonus: String?,
      gamificationLevel: Int,
      skuDescription: String,
      isSubscription: Boolean,
      isSkills: Boolean,
      frequency: String?,
    ): AmazonPayIABFragment = AmazonPayIABFragment().apply {
      arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(ORIGIN_KEY, origin)
        putParcelable(TRANSACTION_DATA_KEY, transactionBuilder)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putString(SKU_DESCRIPTION, skuDescription)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putBoolean(IS_SKILLS, isSkills)
        putString(FREQUENCY, frequency)
      }
    }
  }
}
