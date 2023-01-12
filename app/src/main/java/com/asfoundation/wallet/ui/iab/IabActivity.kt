package com.asfoundation.wallet.ui.iab

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.paypal.PayPalIABFragment
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.asfoundation.wallet.update_required.use_cases.GetAutoUpdateModelUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_iab.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.support_error_layout.*
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class IabActivity : BaseActivity(), IabView, UriNavigator {

  @Inject
  lateinit var billingAnalytics: BillingAnalytics

  @Inject
  lateinit var iabInteract: IabInteract

  @Inject
  lateinit var startVipReferralPollingUseCase: StartVipReferralPollingUseCase

  @Inject
  lateinit var walletBlockedInteract: WalletBlockedInteract

  @Inject
  lateinit var autoUpdateModelUseCase: GetAutoUpdateModelUseCase

  @Inject
  lateinit var hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase

  @Inject
  lateinit var logger: Logger

  private lateinit var presenter: IabPresenter
  private var isBackEnable: Boolean = false
  private var transaction: TransactionBuilder? = null
  private var isBds: Boolean = false
  private var backButtonPress: PublishRelay<Any>? = null
  private var results: PublishRelay<Uri>? = null
  private var developerPayload: String? = null
  private var uri: String? = null
  private var authenticationResultSubject: PublishSubject<Boolean>? = null
  private var errorFromReceiver: String? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    backButtonPress = PublishRelay.create()
    results = PublishRelay.create()
    authenticationResultSubject = PublishSubject.create()
    setContentView(R.layout.activity_iab)
    isBds = intent.getBooleanExtra(IS_BDS_EXTRA, false)
    developerPayload = intent.getStringExtra(DEVELOPER_PAYLOAD)
    uri = intent.getStringExtra(URI)
    transaction = intent.getParcelableExtra(TRANSACTION_EXTRA)
    errorFromReceiver = intent.getStringExtra(ERROR_RECEIVER)
    isBackEnable = true
    presenter = IabPresenter(
      this,
      Schedulers.io(),
      AndroidSchedulers.mainThread(),
      CompositeDisposable(),
      billingAnalytics,
      iabInteract,
      autoUpdateModelUseCase,
      hasRequiredHardUpdateUseCase,
      startVipReferralPollingUseCase,
      logger,
      transaction,
      errorFromReceiver
    )
    presenter.present(savedInstanceState)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data)

    // Adyen's Google Pay currently has a limitation that only receives the response from
    // onActivityResult directly on the Activity, not the fragment. So, the following code is
    // sending the result to the AdyenPaymentFragment to be processed there:
    if (requestCode == AdyenPaymentFragment.GP_CODE) {
      val fragment =
        supportFragmentManager.findFragmentByTag(PaymentMethodsFragment.TAG_GPAY_FRAGMENT)
      fragment?.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onResume() {
    super.onResume()
    //The present is set here due to the Can not perform this action after onSaveInstanceState
    //This assures that doesn't
    presenter.onResume()
  }

  override fun onBackPressed() {
    if (isBackEnable) {
      Bundle().apply {
        putInt(RESPONSE_CODE, RESULT_USER_CANCELED)
        close(this)
      }
      super.onBackPressed()
    } else {
      backButtonPress?.accept(Unit)
    }
  }

  override fun disableBack() {
    isBackEnable = false
  }

  override fun enableBack() {
    isBackEnable = true
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
      sleep(200)
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

  override fun showVerification(isWalletVerified: Boolean) {
    fragment_container.visibility = View.GONE
    val intent = VerificationCreditCardActivity.newIntent(this, isWalletVerified)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
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
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        OnChainBuyFragment.newInstance(
          createBundle(amount),
          intent.data!!.toString(),
          isBds,
          transactionBuilder,
          bonus,
          gamificationLevel
        )
      )
      .commit()
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
    fragmentTag: String?
  ) {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
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
          isSkills = intent.dataString?.contains("&skills") ?: false,
          frequency = frequency,
        ),
        fragmentTag
      )
      .commit()
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
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
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
          isSkills = intent.dataString?.contains("&skills") ?: false,
          frequency = frequency,
        )
      )
      .commit()
  }

  override fun showCarrierBilling(
    currency: String?,
    amount: BigDecimal,
    bonus: BigDecimal?,
    isPreselected: Boolean
  ) {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        CarrierVerifyFragment.newInstance(
          isPreselected,
          transaction!!.domain,
          getOrigin(isBds),
          transaction!!.type,
          intent.dataString,
          currency,
          amount,
          transaction!!.amount(),
          bonus,
          getSkuDescription(),
          transaction!!.skuId
        )
      )
      .addToBackStack(CarrierVerifyFragment.BACKSTACK_NAME)
      .commit()
  }

  override fun showAppcoinsCreditsPayment(
    appcAmount: BigDecimal,
    isPreselected: Boolean,
    gamificationLevel: Int,
    transactionBuilder: TransactionBuilder
  ) {
    supportFragmentManager.beginTransaction().replace(
      R.id.fragment_container,
      AppcoinsRewardsBuyFragment.newInstance(
        appcAmount,
        transactionBuilder,
        intent.data!!.toString(),
        isBds,
        isPreselected,
        gamificationLevel
      )
    )
      .commit()
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
    gamificationLevel: Int
  ) {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        LocalPaymentFragment.newInstance(
          domain,
          skuId,
          originalAmount,
          currency,
          bonus,
          selectedPaymentMethod,
          developerAddress,
          type,
          amount,
          callbackUrl,
          orderReference,
          payload,
          getOrigin(isBds),
          paymentMethodIconUrl,
          paymentMethodLabel,
          async,
          referralUrl,
          gamificationLevel
        )
      )
      .commit()
  }

  override fun showPaymentMethodsView() {
    val isDonation =
      TransactionData.TransactionType.DONATION.name.equals(transaction?.type, ignoreCase = true)
    val isSubscription =
      TransactionData.TransactionType.INAPP_SUBSCRIPTION.name.equals(
        transaction?.type,
        ignoreCase = true
      )
    layout_error.visibility = View.GONE
    fragment_container.visibility = View.VISIBLE

    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        PaymentMethodsFragment.newInstance(
          transaction,
          getSkuDescription(),
          isBds,
          isDonation,
          developerPayload,
          uri,
          intent.dataString,
          isSubscription,
          transaction?.subscriptionPeriod
        )
      )
      .commit()
  }

  override fun showBillingAddress(
    value: BigDecimal,
    currency: String,
    bonus: String,
    appcAmount: BigDecimal,
    targetFragment: Fragment,
    shouldStoreCard: Boolean,
    isStored: Boolean
  ) {
    val isDonation = TransactionData.TransactionType.DONATION.name
      .equals(transaction?.type, ignoreCase = true)

    val fragment = BillingAddressFragment.newInstance(
      transaction!!.skuId,
      getSkuDescription(),
      transaction!!.type,
      transaction!!.domain,
      appcAmount,
      bonus,
      value,
      currency,
      isDonation,
      shouldStoreCard,
      isStored
    )
      .apply {
        @Suppress("DEPRECATION")
        setTargetFragment(targetFragment, TopUpActivity.BILLING_ADDRESS_REQUEST_CODE)
      }

    supportFragmentManager.beginTransaction()
      .add(R.id.fragment_container, fragment)
      .addToBackStack(BillingAddressFragment::class.java.simpleName)
      .commit()
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
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        SharePaymentLinkFragment.newInstance(
          domain,
          skuId,
          originalAmount,
          originalCurrency,
          amount,
          type,
          selectedPaymentMethod
        )
      )
      .commit()
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
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        MergedAppcoinsFragment.newInstance(
          fiatAmount,
          currency,
          bonus,
          transaction.domain,
          getSkuDescription(),
          transaction.amount(),
          isBds,
          isDonation,
          transaction.skuId,
          transaction.type,
          gamificationLevel,
          transaction,
          isSubscription,
          frequency
        )
      )
      .commit()
  }

  override fun showEarnAppcoins(domain: String, skuId: String?, amount: BigDecimal, type: String) {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        EarnAppcoinsFragment.newInstance(domain, skuId, amount, type)
      )
      .commit()
  }

  override fun showUpdateRequiredView() {
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, IabUpdateRequiredFragment())
      .commit()
  }

  override fun showError(@StringRes error: Int) {
    fragment_container.visibility = View.GONE
    layout_error.visibility = View.VISIBLE
    error_message.text = getText(error)
    wallet_logo_layout.visibility = View.GONE
  }

  override fun getSupportClicks(): Observable<Any> =
    Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))

  override fun errorDismisses() = RxView.clicks(error_dismiss)

  override fun launchPerkBonusAndGamificationService(address: String) =
    PerkBonusAndGamificationService.buildService(this, address)

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstance(outState)
  }

  private fun getOrigin(isBds: Boolean): String? = if (transaction!!.origin == null) {
    if (isBds) BDS else null
  } else {
    transaction!!.origin
  }

  private fun createBundle(amount: BigDecimal): Bundle = Bundle().apply {
    putSerializable(TRANSACTION_AMOUNT, amount)
    putString(APP_PACKAGE, transaction!!.domain)
    putString(PRODUCT_NAME, intent.extras!!.getString(PRODUCT_NAME))
    putString(TRANSACTION_DATA, intent.dataString)
    putString(DEVELOPER_PAYLOAD, transaction!!.payload)
  }

  fun isBds() = intent.getBooleanExtra(EXTRA_BDS_IAP, false)

  override fun navigateToUri(url: String) = navigateToWebViewAuthorization(url)

  override fun uriResults() = results

  override fun launchIntent(intent: Intent) = startActivity(intent)

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun backButtonPress() = backButtonPress!!

  override fun successWebViewResult(data: Uri?) =
    results!!.accept(Objects.requireNonNull(data, "Intent data cannot be null!"))

  override fun authenticationResult(success: Boolean) {
    authenticationResultSubject?.onNext(success)
  }

  override fun showTopupFlow() = startActivity(TopUpActivity.newIntent(this))

  override fun onPause() {
    presenter.stop()
    super.onPause()
  }

  override fun onDestroy() {
    backButtonPress = null
    super.onDestroy()
  }

  private fun getSkuDescription(): String = when {
    transaction?.productName.isNullOrEmpty().not() -> transaction?.productName!!
    transaction != null && transaction!!.skuId.isNullOrEmpty().not() -> transaction!!.skuId
    else -> ""
  }

  override fun showAuthenticationActivity() {
    val intent = AuthenticationPromptActivity
      .newIntent(this)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
    @Suppress("DEPRECATION")
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  override fun onAuthenticationResult(): Observable<Boolean> = authenticationResultSubject!!

  companion object {

    const val BILLING_ADDRESS_REQUEST_CODE = 1236
    const val BILLING_ADDRESS_SUCCESS_CODE = 1000
    const val BILLING_ADDRESS_CANCEL_CODE = 1001
    const val URI = "uri"
    const val RESPONSE_CODE = "RESPONSE_CODE"
    const val RESULT_USER_CANCELED = 1
    const val APP_PACKAGE = "app_package"
    const val TRANSACTION_EXTRA = "transaction_extra"
    const val PRODUCT_NAME = "product_name"
    const val TRANSACTION_DATA = "transaction_data"
    const val TRANSACTION_HASH = "transaction_hash"
    const val TRANSACTION_AMOUNT = "transaction_amount"
    const val DEVELOPER_PAYLOAD = "developer_payload"
    const val BDS = "BDS"
    const val WEB_VIEW_REQUEST_CODE = 1234
    const val BLOCKED_WARNING_REQUEST_CODE = 12345
    const val AUTHENTICATION_REQUEST_CODE = 33
    const val IS_BDS_EXTRA = "is_bds_extra"
    const val ERROR_RECEIVER = "error_receiver"
    const val ERROR_RECEIVER_NETWORK = "error_receiver_network"
    const val ERROR_RECEIVER_GENERIC = "error_receiver_generic"

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
