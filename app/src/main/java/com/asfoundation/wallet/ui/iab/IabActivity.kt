package com.asfoundation.wallet.ui.iab

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentFragment
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.asfoundation.wallet.verification.VerificationActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_iab.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class IabActivity : BaseActivity(), IabView, UriNavigator {

  @Inject
  lateinit var billingAnalytics: BillingAnalytics

  @Inject
  lateinit var iabInteract: IabInteract

  @Inject
  lateinit var walletBlockedInteract: WalletBlockedInteract

  @Inject
  lateinit var logger: Logger

  private lateinit var presenter: IabPresenter
  private var walletsBottomSheet: BottomSheetBehavior<View>? = null
  private var shouldUseBottomSheet = false
  private var isBackEnable: Boolean = false
  private var transaction: TransactionBuilder? = null
  private var isBds: Boolean = false
  private var backButtonPress: PublishRelay<Any>? = null
  private var results: PublishRelay<Uri>? = null
  private var developerPayload: String? = null
  private var uri: String? = null
  private var authenticationResultSubject: PublishSubject<Boolean>? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    backButtonPress = PublishRelay.create()
    results = PublishRelay.create()
    transaction = intent.getParcelableExtra(TRANSACTION_EXTRA)
    authenticationResultSubject = PublishSubject.create()
    shouldUseBottomSheet = shouldUseBottomSheet(transaction!!.type)
    setContentView(getLayoutOf(shouldUseBottomSheet))
    isBds = intent.getBooleanExtra(IS_BDS_EXTRA, false)
    developerPayload = intent.getStringExtra(DEVELOPER_PAYLOAD)
    uri = intent.getStringExtra(URI)
    isBackEnable = true
    if (shouldUseBottomSheet) showBottomSheet()
    presenter = IabPresenter(this, Schedulers.io(), AndroidSchedulers.mainThread(),
        CompositeDisposable(), billingAnalytics, iabInteract, transaction)
    presenter.present(savedInstanceState)
  }

  private fun shouldUseBottomSheet(type: String?): Boolean {
    return type == "VOUCHER"
  }

  private fun getLayoutOf(isVoucherTransaction: Boolean): Int {
    return if (isVoucherTransaction) {
      R.layout.activity_bottom_sheet_iab
    } else {
      R.layout.activity_iab
    }
  }

  private fun showBottomSheet() {
    walletsBottomSheet = BottomSheetBehavior.from(fragment_container)
    walletsBottomSheet?.state = BottomSheetBehavior.STATE_EXPANDED
    walletsBottomSheet?.isFitToContents = true
    walletsBottomSheet?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
          walletsBottomSheet?.state = BottomSheetBehavior.STATE_EXPANDED
        }
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    })
  }

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

  override fun showBackupNotification(walletAddress: String) {
    BackupNotificationUtils.showBackupNotification(this, walletAddress)
  }

  override fun finish(bundle: Bundle) {
    if (bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      presenter.handleBackupNotifications(bundle)
      presenter.handlePerkNotifications(bundle)
    } else {
      finishActivity(bundle)
    }
  }

  override fun finishWithError() {
    setResult(Activity.RESULT_CANCELED)
    finish()
  }

  override fun close(bundle: Bundle?) {
    val intent = Intent()
    bundle?.let { intent.putExtras(bundle) }
    setResult(Activity.RESULT_CANCELED, intent)
    finishAfterTransition()
  }

  override fun navigateToWebViewAuthorization(url: String) {
    startActivityForResult(WebViewActivity.newIntent(this, url), WEB_VIEW_REQUEST_CODE)
  }

  override fun showVerification() {
    fragment_container.visibility = View.GONE
    val intent = VerificationActivity.newIntent(this)
        .apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivity(intent)
    finishWithError()
  }

  override fun showOnChain(amount: BigDecimal, isBds: Boolean, bonus: String,
                           gamificationLevel: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, OnChainBuyFragment.newInstance(createBundle(amount),
            intent.data!!.toString(), isBds, transaction, bonus, gamificationLevel))
        .commit()
  }

  override fun showAdyenPayment(fiatAmount: BigDecimal, currency: String, isBds: Boolean,
                                paymentType: PaymentType, bonus: String?, isPreselected: Boolean,
                                iconUrl: String?, gamificationLevel: Int) {
    val transactionData = TransactionPaymentData(fiatAmount, currency, isBds, transaction!!.type,
        transaction!!.domain, getOrigin(isBds), transaction!!.amount(), transaction!!.skuId,
        getSkuDescription(), transaction!!.payload, transaction!!.callbackUrl,
        transaction!!.toAddress(), transaction!!.referrerUrl, transaction!!.orderReference)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AdyenPaymentFragment.newInstance(paymentType, bonus, isPreselected, gamificationLevel,
                transactionData, shouldUseBottomSheet))
        .commit()
  }

  override fun showCarrierBilling(currency: String?, amount: BigDecimal, bonus: BigDecimal?,
                                  isPreselected: Boolean) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierVerifyFragment.newInstance(isPreselected, transaction!!.domain,
                getOrigin(isBds),
                transaction!!.type, intent.dataString, currency, amount, transaction!!.amount(),
                bonus, getSkuDescription(), transaction!!.skuId))
        .addToBackStack(CarrierVerifyFragment.BACKSTACK_NAME)
        .commit()
  }

  override fun showAppcoinsCreditsPayment(appcAmount: BigDecimal, gamificationLevel: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AppcoinsRewardsBuyFragment.newInstance(appcAmount, transaction!!, intent.data!!
                .toString(), isBds, gamificationLevel))
        .commit()
  }

  override fun showLocalPayment(domain: String, skuId: String?, originalAmount: String?,
                                currency: String?, bonus: String?, selectedPaymentMethod: String,
                                developerAddress: String, type: String, amount: BigDecimal,
                                callbackUrl: String?, orderReference: String?, payload: String?,
                                origin: String?,
                                paymentMethodIconUrl: String, paymentMethodLabel: String,
                                async: Boolean, referralUrl: String?, gamificationLevel: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            LocalPaymentFragment.newInstance(domain, skuId, originalAmount, currency, bonus,
                selectedPaymentMethod, developerAddress, type, amount, callbackUrl, orderReference,
                payload, getOrigin(isBds), paymentMethodIconUrl, paymentMethodLabel, async, referralUrl,
                gamificationLevel))
        .commit()
  }

  override fun showPaymentMethodsView() {
    val isDonation = TransactionData.TransactionType.DONATION.name
        .equals(transaction?.type, ignoreCase = true)
    fragment_container.visibility = View.VISIBLE
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PaymentMethodsFragment.newInstance(transaction,
            getSkuDescription(), isBds, isDonation, developerPayload, uri,
            intent.dataString, shouldUseBottomSheet))
        .commit()
  }

  override fun showBillingAddress(value: BigDecimal, currency: String, bonus: String,
                                  appcAmount: BigDecimal, targetFragment: Fragment,
                                  shouldStoreCard: Boolean, isStored: Boolean) {
    val isDonation = TransactionData.TransactionType.DONATION.name
        .equals(transaction?.type, ignoreCase = true)

    val fragment = BillingAddressFragment.newInstance(transaction!!.skuId, getSkuDescription(),
        transaction!!.type, transaction!!.domain, appcAmount, bonus, value, currency, isDonation,
        shouldStoreCard, isStored)
        .apply {
          setTargetFragment(targetFragment, TopUpActivity.BILLING_ADDRESS_REQUEST_CODE)
        }

    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, fragment)
        .addToBackStack(BillingAddressFragment::class.java.simpleName)
        .commit()
  }

  override fun showShareLinkPayment(domain: String, skuId: String?, originalAmount: String?,
                                    originalCurrency: String?, amount: BigDecimal, type: String,
                                    selectedPaymentMethod: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            SharePaymentLinkFragment.newInstance(domain, skuId, originalAmount, originalCurrency,
                amount, type, selectedPaymentMethod))
        .commit()
  }

  override fun showMergedAppcoins(fiatAmount: BigDecimal, currency: String, bonus: String,
                                  isBds: Boolean, isDonation: Boolean, gamificationLevel: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            MergedAppcoinsFragment.newInstance(fiatAmount, currency, bonus, transaction!!.domain,
                getSkuDescription(), transaction!!.amount(), isBds,
                isDonation, transaction!!.skuId, transaction!!.type, gamificationLevel,
                transaction!!))
        .commit()
  }

  override fun showEarnAppcoins(domain: String, skuId: String?, amount: BigDecimal,
                                type: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            EarnAppcoinsFragment.newInstance(domain, skuId, amount, type))
        .commit()
  }

  override fun showUpdateRequiredView() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, IabUpdateRequiredFragment())
        .commit()
  }

  override fun launchPerkBonusAndGamificationService(address: String) {
    PerkBonusAndGamificationService.buildService(this, address)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstance(outState)
  }

  private fun getOrigin(isBds: Boolean): String? {
    return if (transaction!!.origin == null) {
      if (isBds) BDS else null
    } else {
      transaction!!.origin
    }
  }

  private fun createBundle(amount: BigDecimal): Bundle {
    return Bundle().apply {
      putSerializable(TRANSACTION_AMOUNT, amount)
      putString(APP_PACKAGE, transaction!!.domain)
      putString(PRODUCT_NAME, intent.extras!!.getString(PRODUCT_NAME))
      putString(TRANSACTION_DATA, intent.dataString)
      putString(DEVELOPER_PAYLOAD, transaction!!.payload)
    }
  }

  fun isBds() = intent.getBooleanExtra(EXTRA_BDS_IAP, false)

  override fun navigateToUri(url: String) {
    navigateToWebViewAuthorization(url)
  }

  override fun uriResults() = results

  override fun launchIntent(intent: Intent) {
    startActivity(intent)
  }

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun backButtonPress() = backButtonPress!!

  override fun successWebViewResult(data: Uri?) {
    results!!.accept(Objects.requireNonNull(data, "Intent data cannot be null!"))
  }

  override fun authenticationResult(success: Boolean) {
    authenticationResultSubject?.onNext(success)
  }

  override fun onPause() {
    presenter.stop()
    super.onPause()
  }

  override fun onDestroy() {
    backButtonPress = null
    super.onDestroy()
  }

  private fun getSkuDescription(): String {
    return when {
      transaction?.productName.isNullOrEmpty()
          .not() -> transaction?.productName!!
      transaction != null && transaction!!.skuId.isNullOrEmpty()
          .not() -> transaction!!.skuId
      else -> ""
    }
  }

  override fun showAuthenticationActivity() {
    val intent = AuthenticationPromptActivity.newIntent(this)
        .apply { intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  override fun onAuthenticationResult(): Observable<Boolean> {
    return authenticationResultSubject!!
  }

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

    @JvmStatic
    fun newIntent(activity: Activity, previousIntent: Intent?, transaction: TransactionBuilder,
                  isBds: Boolean?, developerPayload: String?): Intent {
      return Intent(activity, IabActivity::class.java)
          .apply {
            if (previousIntent != null) {
              data = previousIntent.data
              if (previousIntent.extras != null) {
                putExtras(previousIntent.extras!!)
              }
            }
            putExtra(TRANSACTION_EXTRA, transaction)
            putExtra(IS_BDS_EXTRA, isBds)
            putExtra(DEVELOPER_PAYLOAD, developerPayload)
            putExtra(URI, data?.toString() ?: "")
            putExtra(APP_PACKAGE, transaction.domain)
          }
    }
  }
}