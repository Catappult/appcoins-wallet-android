package com.asfoundation.wallet.ui.iab

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.core.utils.android_common.NetworkMonitor
import com.appcoins.wallet.core.utils.android_common.extensions.getParcelable
import com.appcoins.wallet.ui.widgets.NoNetworkCard
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityIabBinding
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.AmazonPayIABFragment
import com.asfoundation.wallet.billing.googlepay.GooglePayWebFragment
import com.asfoundation.wallet.billing.mipay.MiPayFragment
import com.asfoundation.wallet.billing.paypal.PayPalIABFragment
import com.asfoundation.wallet.billing.sandbox.SandboxFragment
import com.asfoundation.wallet.billing.vkpay.VkPaymentIABFragment
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.Objects
import javax.inject.Inject


@AndroidEntryPoint
class IabActivity : BaseActivity(), IabView, UriNavigator {

  @Inject
  lateinit var networkMonitor: NetworkMonitor

  @Inject
  lateinit var presenter: IabPresenter

  private var isBackEnable: Boolean = true

  private val backButtonPress by lazy { PublishRelay.create<Any>() }
  private val results by lazy { PublishRelay.create<Uri>() }
  private val authenticationResultSubject by lazy { PublishSubject.create<Boolean>() }

  private val transaction by lazy { getParcelable<TransactionBuilder>(TRANSACTION_EXTRA) }
  private val isBds by lazy { intent.getBooleanExtra(IS_BDS_EXTRA, false) }
  private val developerPayload by lazy { intent.getStringExtra(DEVELOPER_PAYLOAD) }
  private val uri by lazy { intent.getStringExtra(URI) }
  private val errorFromReceiver by lazy { intent.getStringExtra(ERROR_RECEIVER) }

  override var webViewResultCode: String? = null

  private val binding by viewBinding(ActivityIabBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_iab)
    presenter.init(
      view = this,
      transaction = transaction,
      errorFromReceiver = errorFromReceiver
    )
    presenter.present(savedInstanceState)
  }

  @Deprecated("Deprecated in Java")
  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data)
  }

  override fun onResume() {
    super.onResume()
    //The present is set here due to the Can not perform this action after onSaveInstanceState
    //This assures that doesn't
    presenter.onResume()
  }

  @Suppress("DEPRECATION")
  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    if (isBackEnable) {
      val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
      if (fragment is OnBackPressedListener) {
        (fragment as OnBackPressedListener).onBackPressed()
      } else {
        bundleOf(RESPONSE_CODE to RESULT_USER_CANCELED).apply { close(this) }
        super.onBackPressed()
      }
    } else {
      backButtonPress?.accept(Unit)
    }
  }

  override fun setBackEnable(enable: Boolean) {
    isBackEnable = enable
  }

  override fun navigateBack() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStack()
    }
  }

  override fun finishActivity(data: Bundle) {
    presenter.savePreselectedPaymentMethod(data)
    data.remove(PRE_SELECTED_PAYMENT_METHOD_KEY)
    setResult(Activity.RESULT_OK, Intent().putExtras(data))
    finish()
  }

  override fun showBackupNotification(walletAddress: String) =
    BackupNotificationUtils.showBackupNotification(this, walletAddress)

  override fun finish(bundle: Bundle) =
    if (bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      presenter.handleBackupNotifications(bundle)
      // Sleep added as a temporary fix to launch the notifications separately.
      // When both notifications are launched together then only one shows up
      presenter.handlePerkNotifications(bundle)
    } else {
      finishActivity(bundle)
    }

  override fun finishWithError() {
    setResult(Activity.RESULT_CANCELED)
    finish()
  }

  override fun close(bundle: Bundle?) {
    val intent = Intent()
    bundle?.let { intent.putExtras(bundle) }
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  override fun navigateToWebViewAuthorization(url: String) =
    @Suppress("DEPRECATION")
    startActivityForResult(WebViewActivity.newIntent(this, url), WEB_VIEW_REQUEST_CODE)

  override fun showCreditCardVerification(isWalletVerified: Boolean) {
    binding.fragmentContainer.visibility = View.GONE
    val intent = VerificationCreditCardActivity.newIntent(this, isWalletVerified)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivity(intent)
    finishWithError()
  }

  override fun showPayPalVerification() {
    binding.fragmentContainer.visibility = View.GONE
    val intent = MainActivity.newIntent(
      context = this,
      supportNotificationClicked = false,
      isPayPalVerificationRequired = true
    ).apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }

    startActivity(intent)
    finishWithError()
  }

  override fun showOnChain(
    amount: BigDecimal,
    isBds: Boolean,
    bonus: String,
    gamificationLevel: Int,
    transactionBuilder: TransactionBuilder
  ) {
    replaceFragment(
      OnChainBuyFragment.newInstance(
        createBundle(amount),
        intent.data!!.toString(),
        isBds,
        transactionBuilder,
        bonus,
        gamificationLevel
      )
    )
  }

  override fun showAdyenPayment(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ) {
    replaceFragment(
      AdyenPaymentFragment.newInstance(
        paymentType = paymentType,
        origin = getOrigin(isBds),
        transactionBuilder = transaction!!,
        amount = amount,
        currency = currency,
        bonus = bonus,
        isPreSelected = isPreselected,
        gamificationLevel = gamificationLevel,
        skuDescription = getSkuDescription(),
        isSubscription = isSubscription,
        isSkills = intent.dataString?.contains(SKILLS_TAG) ?: false,
        frequency = frequency,
        paymentStateEnum = null,
        isFreeTrial = isFreeTrial,
        freeTrialDuration = freeTrialDuration,
        subscriptionStartingDate = subscriptionStartingDate,
      )
    )
  }

  override fun showPayPalV2(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  ) {
    replaceFragment(
      PayPalIABFragment.newInstance(
        paymentType = paymentType,
        origin = getOrigin(isBds),
        transactionBuilder = transaction!!,
        amount = amount,
        currency = currency,
        bonus = bonus,
        isPreSelected = isPreselected,
        gamificationLevel = gamificationLevel,
        skuDescription = getSkuDescription(),
        isSubscription = isSubscription,
        isSkills = intent.dataString?.contains(SKILLS_TAG) ?: false,
        frequency = frequency,
      )
    )
  }

  override fun showSandbox(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  ) {
    replaceFragment(
      SandboxFragment.newInstance(
        paymentType = paymentType,
        origin = getOrigin(isBds),
        transactionBuilder = transaction!!,
        amount = amount,
        currency = currency,
        bonus = bonus,
        isPreSelected = isPreselected,
        gamificationLevel = gamificationLevel,
        skuDescription = getSkuDescription(),
        isSubscription = isSubscription,
        isSkills = intent.dataString?.contains("&skills") ?: false,
        frequency = frequency,
      )
    )
  }

  override fun showVkPay(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  ) {
    addFragment(
      fragment = VkPaymentIABFragment().apply {
        arguments = bundleOf(
          VkPaymentIABFragment.PAYMENT_TYPE_KEY to paymentType.name,
          VkPaymentIABFragment.ORIGIN_KEY to getOrigin(isBds),
          VkPaymentIABFragment.TRANSACTION_DATA_KEY to transaction!!,
          VkPaymentIABFragment.AMOUNT_KEY to amount,
          VkPaymentIABFragment.CURRENCY_KEY to currency,
          VkPaymentIABFragment.BONUS_KEY to bonus,
          VkPaymentIABFragment.SKU_DESCRIPTION to getSkuDescription(),
          VkPaymentIABFragment.IS_SKILLS to intent.dataString?.contains(SKILLS_TAG),
          VkPaymentIABFragment.FREQUENCY to frequency
        )
      },
      addToBackStackName = VkPaymentIABFragment::class.java.simpleName
    )
  }

  override fun showAmazonPay(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  ) {
    replaceFragment(
      AmazonPayIABFragment.newInstance(
        paymentType = paymentType,
        origin = getOrigin(isBds),
        transactionBuilder = transaction!!,
        amount = amount,
        currency = currency,
        bonus = bonus,
        gamificationLevel = gamificationLevel,
        skuDescription = getSkuDescription(),
        isSubscription = isSubscription,
        isSkills = intent.dataString?.contains(SKILLS_TAG) ?: false,
        frequency = frequency,
      )
    )
  }


  override fun showGooglePayWeb(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  ) {
    replaceFragment(
      GooglePayWebFragment.newInstance(
        paymentType = paymentType,
        origin = getOrigin(isBds),
        transactionBuilder = transaction!!,
        amount = amount,
        currency = currency,
        bonus = bonus,
        isPreSelected = isPreselected,
        gamificationLevel = gamificationLevel,
        skuDescription = getSkuDescription(),
        isSubscription = isSubscription,
        isSkills = intent.dataString?.contains(SKILLS_TAG) ?: false,
        frequency = frequency,
      )
    )
  }

  override fun showMiPayWeb(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    bonus: String?
  ) {
    addFragment(
      fragment = MiPayFragment().apply {
        arguments = bundleOf(
          MiPayFragment.TRANSACTION_DATA_KEY to transaction!!,
          MiPayFragment.AMOUNT_KEY to amount,
          MiPayFragment.CURRENCY_KEY to currency,
          MiPayFragment.BONUS_KEY to bonus,
        )
      },
      addToBackStackName = MiPayFragment::class.java.simpleName
    )
  }

  override fun showCarrierBilling(
    currency: String?,
    amount: BigDecimal,
    bonus: BigDecimal?,
    isPreselected: Boolean
  ) {
    replaceFragment(
      fragment = CarrierVerifyFragment.newInstance(
        preSelected = isPreselected,
        domain = transaction!!.domain,
        origin = getOrigin(isBds),
        transactionType = transaction!!.type,
        transactionData = intent.dataString,
        currency = currency,
        amount = amount,
        appcAmount = transaction!!.amount(),
        bonus = bonus,
        skuDescription = getSkuDescription(),
        skuId = transaction!!.skuId
      ),
      addToBackStackName = CarrierVerifyFragment.BACKSTACK_NAME
    )
  }

  override fun showAppcoinsCreditsPayment(
    appcAmount: BigDecimal,
    isPreselected: Boolean,
    gamificationLevel: Int,
    transactionBuilder: TransactionBuilder
  ) {
    replaceFragment(
      AppcoinsRewardsBuyFragment.newInstance(
        amount = appcAmount,
        transactionBuilder = transactionBuilder,
        uri = intent.data!!.toString(),
        isBds = isBds,
        isPreSelected = isPreselected,
        gamificationLevel = gamificationLevel
      )
    )
  }

  override fun showLocalPayment(
    domain: String,
    skuId: String?,
    originalAmount: String?,
    currency: String?,
    bonus: String?,
    selectedPaymentMethod: String,
    developerAddress: String,
    type: String,
    amount: BigDecimal,
    callbackUrl: String?,
    orderReference: String?,
    payload: String?,
    origin: String?,
    paymentMethodIconUrl: String,
    paymentMethodLabel: String,
    async: Boolean,
    referralUrl: String?,
    gamificationLevel: Int,
    guestWalletId: String?
  ) {
    replaceFragment(
      LocalPaymentFragment.newInstance(
        domain = domain,
        skudId = skuId,
        originalAmount = originalAmount,
        currency = currency,
        bonus = bonus,
        selectedPaymentMethod = selectedPaymentMethod,
        developerAddress = developerAddress,
        type = type,
        amount = amount,
        callbackUrl = callbackUrl,
        orderReference = orderReference,
        payload = payload,
        origin = getOrigin(isBds),
        paymentMethodIconUrl = paymentMethodIconUrl,
        paymentMethodLabel = paymentMethodLabel,
        async = async,
        referralUrl = referralUrl,
        gamificationLevel = gamificationLevel,
        guestWalletId = guestWalletId
      )
    )
  }


  override fun showPaymentMethodsView() {
    val isDonation =
      TransactionData.TransactionType.DONATION.name.equals(transaction?.type, ignoreCase = true)
    val isSubscription =
      TransactionData.TransactionType.INAPP_SUBSCRIPTION.name.equals(
        transaction?.type,
        ignoreCase = true
      )
    binding.layoutError.visibility = View.GONE
    binding.fragmentContainer.visibility = View.VISIBLE

    replaceFragment(
      PaymentMethodsFragment.newInstance(
        transaction = transaction,
        productName = getSkuDescription(),
        isBds = isBds,
        isDonation = isDonation,
        developerPayload = developerPayload,
        uri = uri,
        transactionData = intent.dataString,
        isSubscription = isSubscription,
        frequency = transaction?.subscriptionPeriod
      )
    )
  }

  override fun showShareLinkPayment(
    domain: String,
    skuId: String?,
    originalAmount: String?,
    originalCurrency: String?,
    amount: BigDecimal,
    type: String,
    selectedPaymentMethod: String
  ) {
    replaceFragment(
      SharePaymentLinkFragment.newInstance(
        domain = domain,
        skuId = skuId,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        amount = amount,
        type = type,
        paymentMethod = selectedPaymentMethod
      )
    )
  }

  override fun showMergedAppcoins(
    fiatAmount: BigDecimal,
    currency: String,
    bonus: String,
    isBds: Boolean,
    isDonation: Boolean,
    gamificationLevel: Int,
    transaction: TransactionBuilder,
    isSubscription: Boolean,
    frequency: String?
  ) {
    replaceFragment(
      MergedAppcoinsFragment.newInstance(
        fiatAmount = fiatAmount,
        currency = currency,
        bonus = bonus,
        appName = transaction.domain,
        productName = getSkuDescription(),
        appcAmount = transaction.amount(),
        isBds = isBds,
        isDonation = isDonation,
        skuId = transaction.skuId,
        transactionType = transaction.type,
        gamificationLevel = gamificationLevel,
        transactionBuilder = transaction,
        isSubscription = isSubscription,
        frequency = frequency
      )
    )
  }

  override fun showEarnAppcoins(domain: String, skuId: String?, amount: BigDecimal, type: String) {
    replaceFragment(
      EarnAppcoinsFragment.newInstance(
        domain = domain,
        skuId = skuId,
        amount = amount,
        type = type
      )
    )
  }

  override fun showUpdateRequiredView() {
    replaceFragment(IabUpdateRequiredFragment())
  }

  @SuppressLint("CommitTransaction")
  private fun addFragment(fragment: Fragment, addToBackStackName: String? = null) {
    supportFragmentManager.beginTransaction()
      .add(R.id.fragment_container, fragment)
      .apply { addToBackStackName?.let { addToBackStack(it) } }
      .commit()
  }

  @SuppressLint("CommitTransaction")
  private fun replaceFragment(fragment: Fragment, addToBackStackName: String? = null) {
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .apply { addToBackStackName?.let { addToBackStack(it) } }
      .commit()
  }

  override fun showError(@StringRes error: Int) {
    binding.fragmentContainer.visibility = View.GONE
    binding.layoutError.visibility = View.VISIBLE
    binding.iabErrorLayout.genericErrorLayout.errorMessage.text = getText(error)
  }

  override fun showNoNetworkError() {
    binding.fragmentContainer.visibility = View.GONE
    binding.layoutError.visibility = View.VISIBLE
    binding.walletLogoLayout.iapComposeView.visibility = View.GONE
    binding.iabErrorLayout.genericErrorLayout.root.visibility = View.GONE
    binding.iabErrorLayout.noNetworkErrorLayout.root.visibility = View.VISIBLE
    binding.iabErrorLayout.errorDismiss.visibility = View.GONE
    binding.iabErrorLayout.retryButton.visibility = View.VISIBLE
  }

  override fun handleConnectionObserver() {
    binding.walletLogoLayout.iapComposeView.setContent {
      ConnectionAlert(networkMonitor.isConnected.collectAsState(true).value)
    }
  }

  override fun getSupportClicks(): Observable<Any> =
    Observable.merge(
      RxView.clicks(binding.iabErrorLayout.genericErrorLayout.layoutSupportLogo),
      RxView.clicks(binding.iabErrorLayout.genericErrorLayout.layoutSupportIcn)
    )

  override fun errorDismisses() = RxView.clicks(binding.iabErrorLayout.errorDismiss)

  override fun errorTryAgain() = RxView.clicks(binding.iabErrorLayout.retryButton as View)

  override fun launchPerkBonusAndGamificationService(address: String) =
    PerkBonusAndGamificationService.buildService(this, address)

  override fun showRebrandingBanner() {
    binding.walletLogoLayout.root.visibility = View.GONE
    binding.walletLogoRebrandingLayout.root.visibility = View.VISIBLE
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstance(outState)
  }

  private fun getOrigin(isBds: Boolean): String? = if (transaction!!.origin == null) {
    if (isBds) BDS else null
  } else {
    transaction!!.origin
  }

  private fun createBundle(amount: BigDecimal) = bundleOf(
    TRANSACTION_AMOUNT to amount,
    APP_PACKAGE to transaction!!.domain,
    PRODUCT_NAME to intent.extras!!.getString(PRODUCT_NAME),
    TRANSACTION_DATA to intent.dataString,
    DEVELOPER_PAYLOAD to transaction!!.payload,
  )

  override fun navigateToUri(url: String) = navigateToWebViewAuthorization(url)

  override fun uriResults(): PublishRelay<Uri> = results

  override fun launchIntent(intent: Intent) = startActivity(intent)

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun backButtonPress(): PublishRelay<Any> = backButtonPress

  override fun successWebViewResult(data: Uri?) =
    results.accept(Objects.requireNonNull(data, "Intent data cannot be null!"))

  override fun authenticationResult(success: Boolean) {
    authenticationResultSubject.onNext(success)
  }

  override fun showTopupFlow() = startActivity(TopUpActivity.newIntent(this))

  override fun onPause() {
    presenter.stop()
    super.onPause()
  }

  private fun getSkuDescription(): String = when {
    transaction?.productName.isNullOrEmpty().not() -> transaction?.productName!!
    transaction != null && transaction!!.skuId.isNullOrEmpty().not() -> transaction!!.skuId
    else -> ""
  }

  @Suppress("DEPRECATION")
  override fun showAuthenticationActivity() {
    val intent = AuthenticationPromptActivity.newIntent(this)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  override fun onAuthenticationResult(): Observable<Boolean> = authenticationResultSubject

  @Composable
  fun ConnectionAlert(isConnected: Boolean) {
    if (!isConnected && !binding.iabErrorLayout.noNetworkErrorLayout.root.isVisible) NoNetworkCard()
  }

  companion object {
    private const val RESULT_USER_CANCELED = 1
    private const val BDS = "BDS"
    private const val IS_BDS_EXTRA = "is_bds_extra"
    private const val ERROR_RECEIVER = "error_receiver"
    private const val ERROR_RECEIVER_GENERIC = "error_receiver_generic"
    private const val SKILLS_TAG = "&skills"
    const val BILLING_ADDRESS_REQUEST_CODE = 1236
    const val BILLING_ADDRESS_SUCCESS_CODE = 1000
    const val URI = "uri"
    const val RESPONSE_CODE = "RESPONSE_CODE"
    const val APP_PACKAGE = "app_package"
    const val TRANSACTION_EXTRA = "transaction_extra"
    const val PRODUCT_NAME = "product_name"
    const val TRANSACTION_DATA = "transaction_data"
    const val TRANSACTION_HASH = "transaction_hash"
    const val TRANSACTION_AMOUNT = "transaction_amount"
    const val DEVELOPER_PAYLOAD = "developer_payload"
    const val WEB_VIEW_REQUEST_CODE = 1234
    const val BLOCKED_WARNING_REQUEST_CODE = 12345
    const val AUTHENTICATION_REQUEST_CODE = 33
    const val ERROR_RECEIVER_NETWORK = "error_receiver_network"

    @JvmStatic
    fun newIntent(
      activity: Activity,
      previousIntent: Intent,
      transaction: TransactionBuilder,
      isBds: Boolean,
      developerPayload: String?
    ): Intent = Intent(activity, IabActivity::class.java)
      .apply {
        data = previousIntent.data
        if (previousIntent.extras != null) {
          putExtras(previousIntent.extras!!)
        }
        putExtra(TRANSACTION_EXTRA, transaction)
        putExtra(IS_BDS_EXTRA, isBds)
        putExtra(DEVELOPER_PAYLOAD, developerPayload)
        putExtra(URI, data!!.toString())
        putExtra(APP_PACKAGE, transaction.domain)
      }

    @JvmStatic
    fun newIntent(
      activity: Activity,
      previousIntent: Intent,
      errorFromReceiver: String?
    ): Intent = Intent(activity, IabActivity::class.java)
      .apply {
        data = previousIntent.data
        if (previousIntent.extras != null) {
          putExtras(previousIntent.extras!!)
        }
        putExtra(ERROR_RECEIVER, (errorFromReceiver ?: ERROR_RECEIVER_GENERIC))
      }
  }
}