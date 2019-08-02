package com.asfoundation.wallet.ui.iab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.EXTRA_BDS_IAP
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.WebViewActivity.SUCCESS
import com.asfoundation.wallet.ui.iab.share.SharePaymentLinkFragment
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

/**
 * Created by franciscocalado on 20/07/2018.
 */

class IabActivity : BaseActivity(), IabView, UriNavigator {


  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor
  private var isBackEnable: Boolean = false
  private var presenter: IabPresenter? = null
  private var skuDetails: Bundle? = null
  private var transaction: TransactionBuilder? = null
  private var isBds: Boolean = false
  private var results: PublishRelay<Uri>? = null
  private var developerPayload: String? = null
  private var uri: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    results = PublishRelay.create()
    setContentView(R.layout.activity_iab)
    isBds = intent.getBooleanExtra(IS_BDS_EXTRA, false)
    developerPayload = intent.getStringExtra(DEVELOPER_PAYLOAD)
    uri = intent.getStringExtra(URI)
    transaction = intent.getParcelableExtra(TRANSACTION_EXTRA)
    isBackEnable = true
    presenter = IabPresenter(this)

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SKU_DETAILS)) {
        skuDetails = savedInstanceState.getBundle(SKU_DETAILS)
      }
    }
    presenter!!.present(savedInstanceState)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == WEB_VIEW_REQUEST_CODE) {
      if (resultCode == WebViewActivity.FAIL) {
        finish()
      } else if (resultCode == SUCCESS) {
        results!!.accept(Objects.requireNonNull(data!!.data, "Intent data cannot be null!"))
      }
    }
  }

  override fun onBackPressed() {
    if (isBackEnable) {
      val bundle = Bundle()
      bundle.putInt(RESPONSE_CODE, RESULT_USER_CANCELED)
      close(bundle)
      super.onBackPressed()
    }
  }

  override fun disableBack() {
    isBackEnable = false
  }

  override fun finish(bundle: Bundle) {
    setResult(Activity.RESULT_OK, Intent().putExtras(bundle))
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
        bundle.getString(PRE_SELECTED_PAYMENT_METHOD_KEY))
    finish()
  }

  override fun showError() {
    setResult(Activity.RESULT_CANCELED)
    finish()
  }

  override fun close(bundle: Bundle?) {
    val intent = Intent()
    bundle?.let { intent.putExtras(bundle) }
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  override fun navigateToWebViewAuthorization(url: String) {
    startActivityForResult(WebViewActivity.newIntent(this, url), WEB_VIEW_REQUEST_CODE)
  }

  override fun showOnChain(amount: BigDecimal, isBds: Boolean, bonus: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, OnChainBuyFragment.newInstance(createBundle(amount),
            intent.data!!
                .toString(), isBds, transaction, bonus))
        .commit()
  }

  override fun showAdyenPayment(amount: BigDecimal, currency: String?, isBds: Boolean,
                                paymentType: PaymentType, bonus: String?, isPreselected: Boolean,
                                iconUrl: String?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AdyenAuthorizationFragment.newInstance(transaction!!.skuId, transaction!!.type,
                getOrigin(isBds), paymentType, transaction!!.domain, intent.dataString,
                transaction!!.amount(), currency, developerPayload, bonus, isPreselected, iconUrl))
        .commit()
  }

  override fun showAppcoinsCreditsPayment(amount: BigDecimal) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AppcoinsRewardsBuyFragment.newInstance(amount, transaction, intent.data!!
                .toString(), intent.extras!!
                .getString(PRODUCT_NAME, ""), isBds))
        .commit()
  }

  override fun showLocalPayment(domain: String, skuId: String?, originalAmount: String?,
                                currency: String?,
                                bonus: String?, selectedPaymentMethod: String,
                                developerAddress: String,
                                type: String, amount: BigDecimal, callbackUrl: String?,
                                orderReference: String?, payload: String?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            LocalPaymentFragment.newInstance(domain, skuId, originalAmount, currency, bonus,
                selectedPaymentMethod, developerAddress, type, amount, callbackUrl, orderReference,
                payload))
        .commit()
  }

  override fun showPaymentMethodsView() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PaymentMethodsFragment.newInstance(transaction,
            intent.extras!!
                .getString(PRODUCT_NAME), isBds, developerPayload, uri, intent.dataString))
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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    results!!.accept(Objects.requireNonNull(intent.data, "Intent data cannot be null!"))
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putBundle(SKU_DETAILS, skuDetails)
  }

  private fun getOrigin(isBds: Boolean): String? {
    return if (transaction!!.origin == null) {
      if (isBds) BDS else null
    } else {
      transaction!!.origin
    }
  }

  private fun createBundle(amount: BigDecimal): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(TRANSACTION_AMOUNT, amount)
    bundle.putString(APP_PACKAGE, transaction!!.domain)
    bundle.putString(PRODUCT_NAME, intent.extras!!
        .getString(PRODUCT_NAME))
    bundle.putString(TRANSACTION_DATA, intent.dataString)
    bundle.putString(DEVELOPER_PAYLOAD, transaction!!.payload)
    skuDetails = bundle
    return bundle
  }

  fun isBds(): Boolean {
    return intent.getBooleanExtra(EXTRA_BDS_IAP, false)
  }

  override fun navigateToUri(url: String) {
    navigateToWebViewAuthorization(url)
  }

  override fun uriResults(): Observable<Uri>? {
    return results
  }

  companion object {

    const val URI = "uri"
    const val RESPONSE_CODE = "RESPONSE_CODE"
    const val RESULT_USER_CANCELED = 1
    const val SKU_DETAILS = "sku_details"
    const val APP_PACKAGE = "app_package"
    const val TRANSACTION_EXTRA = "transaction_extra"
    const val PRODUCT_NAME = "product_name"
    const val TRANSACTION_DATA = "transaction_data"
    const val TRANSACTION_HASH = "transaction_hash"
    const val TRANSACTION_AMOUNT = "transaction_amount"
    const val TRANSACTION_CURRENCY = "transaction_currency"
    const val DEVELOPER_PAYLOAD = "developer_payload"
    const val BDS = "BDS"
    const val WEB_VIEW_REQUEST_CODE = 1234
    const val IS_BDS_EXTRA = "is_bds_extra"

    @JvmStatic
    fun newIntent(activity: Activity, previousIntent: Intent, transaction: TransactionBuilder,
                  isBds: Boolean?, developerPayload: String?): Intent {
      val intent = Intent(activity, IabActivity::class.java)
      intent.data = previousIntent.data
      if (previousIntent.extras != null) {
        intent.putExtras(previousIntent.extras!!)
      }
      intent.putExtra(TRANSACTION_EXTRA, transaction)
      intent.putExtra(IS_BDS_EXTRA, isBds)
      intent.putExtra(DEVELOPER_PAYLOAD, developerPayload)
      intent.putExtra(URI, intent.data!!.toString())
      intent.putExtra(APP_PACKAGE, transaction.domain)
      return intent
    }

    @JvmStatic
    fun newIntent(activity: Activity, url: String): Intent {
      val intent = Intent(activity, IabActivity::class.java)
      intent.data = Uri.parse(url)
      return intent
    }
  }
}