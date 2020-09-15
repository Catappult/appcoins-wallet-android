package com.asfoundation.wallet.billing.adyen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.R
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.adyen_credit_card_layout.*
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import java.math.BigDecimal
import javax.inject.Inject

class BillingAddressFragment : DaggerFragment() {

  @Inject
  lateinit var billingAddressInteractor: BillingAddressInteractor

  @Inject
  lateinit var formatter: CurrencyFormatUtils


  @Inject
  lateinit var logger: Logger

  private lateinit var iabView: IabView
  private lateinit var disposables: CompositeDisposable
  private lateinit var presenter: BillingAddressPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    disposables = CompositeDisposable()
    presenter = BillingAddressPresenter(this, disposables, AndroidSchedulers.mainThread(),
        Schedulers.io())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_billing_address, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi(view)
    presenter.present(savedInstanceState)
  }


  private fun setupUi(view: View) {
    handleBuyButtonText()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "adyen payment fragment must be attached to IAB activity" }
    iabView = context
  }

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence? {
    val packageManager = context!!.packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun showBonus() {
    if (bonus.isNotEmpty()) {
      bonus_layout?.visibility = VISIBLE
      bonus_layout_pre_selected?.visibility = VISIBLE
      bonus_msg?.visibility = VISIBLE
      bonus_msg_pre_selected?.visibility = VISIBLE
      bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      bonus_layout?.visibility = GONE
      bonus_layout_pre_selected?.visibility = GONE
      bonus_msg?.visibility = GONE
      bonus_msg_pre_selected?.visibility = GONE
    }
  }

  private fun handleBuyButtonText() {
    if (transactionType.equals(TransactionData.TransactionType.DONATION.name, ignoreCase = true)) {
      buy_button.setText(R.string.action_donate)
    } else {
      buy_button.setText(R.string.action_buy)
    }
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    private const val TRANSACTION_TYPE_KEY = "type"
    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val DOMAIN_KEY = "domain"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"

    @JvmStatic
    fun newInstance(): BillingAddressFragment {
      val fragment = BillingAddressFragment()
      fragment.arguments = Bundle().apply {
      }
      return fragment
    }
  }

  private val transactionType: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_TYPE_KEY)) {
      arguments!!.getString(TRANSACTION_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("transaction type data not found")
    }
  }

  private val paymentType: String by lazy {
    if (arguments!!.containsKey(PAYMENT_TYPE_KEY)) {
      arguments!!.getString(PAYMENT_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("payment type data not found")
    }
  }

  private val domain: String by lazy {
    if (arguments!!.containsKey(DOMAIN_KEY)) {
      arguments!!.getString(DOMAIN_KEY, "")
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }

  private val origin: String? by lazy {
    if (arguments!!.containsKey(ORIGIN_KEY)) {
      arguments!!.getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionData: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_DATA_KEY)) {
      arguments!!.getString(TRANSACTION_DATA_KEY, "")
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val appcAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(APPC_AMOUNT_KEY)) {
      arguments!!.getSerializable(APPC_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("appc amount data not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (arguments!!.containsKey(CURRENCY_KEY)) {
      arguments!!.getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val bonus: String by lazy {
    if (arguments!!.containsKey(BONUS_KEY)) {
      arguments!!.getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val isPreSelected: Boolean by lazy {
    if (arguments!!.containsKey(PRE_SELECTED_KEY)) {
      arguments!!.getBoolean(PRE_SELECTED_KEY)
    } else {
      throw IllegalArgumentException("pre selected data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (arguments!!.containsKey(GAMIFICATION_LEVEL)) {
      arguments!!.getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }

  private val skuDescription: String by lazy {
    if (arguments!!.containsKey(SKU_DESCRIPTION)) {
      arguments!!.getString(SKU_DESCRIPTION, "")
    } else {
      throw IllegalArgumentException("sku description data not found")
    }
  }
}