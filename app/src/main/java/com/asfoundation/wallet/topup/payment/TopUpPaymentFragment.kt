package com.asfoundation.wallet.topup.payment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.DeveloperPurchase
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.IabActivity.*
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_top_up.*
import java.io.IOException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

class TopUpPaymentFragment : DaggerFragment(), TopUpPaymentView {
  private val compositeDisposable = CompositeDisposable()
  @Inject
  internal lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor
  @Inject
  internal lateinit var bdsApi: RemoteRepository.BdsApi
  @Inject
  internal lateinit var walletService: WalletService
  @Inject
  internal lateinit var bdsPendingTransactionService: BdsPendingTransactionService
  @Inject
  internal lateinit var bdsApiSecondary: BdsApiSecondary
  @Inject
  internal lateinit var billing: Billing
  private var extras: Bundle? = null
  private var activityView: TopUpActivityView? = null
  private lateinit var presenter: TopUpPaymentPresenter
  private lateinit var itemFinalPrice: TextView
  private lateinit var fiatValue: FiatValue
  private lateinit var errorView: View
  private lateinit var errorMessage: TextView
  private lateinit var setupSubject: PublishSubject<Boolean>
  private lateinit var paymentType: PaymentType

  val appPackage: String?
    get() {
      if (extras!!.containsKey(APP_PACKAGE)) {
        return extras!!.getString(APP_PACKAGE)
      }
      throw IllegalArgumentException("previous app package name not found")
    }

  val productName: String?
    get() {
      if (extras!!.containsKey(PRODUCT_NAME)) {
        return extras!!.getString(PRODUCT_NAME)
      }
      throw IllegalArgumentException("product name not found")
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setupSubject = PublishSubject.create()
    extras = arguments?.getBundle("extras")

    val isBds = arguments!!.getBoolean("isBds")

    if (arguments != null) {
      paymentType = PaymentType.valueOf(arguments!!.getString("paymentType"))
    }
    presenter = TopUpPaymentPresenter(this, appPackage, inAppPurchaseInteractor,
        AndroidSchedulers.mainThread(), CompositeDisposable(),
        inAppPurchaseInteractor.billingMessagesMapper, bdsPendingTransactionService, billing, isBds,
        extras!!.getString(TRANSACTION_DATA))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return inflater.inflate(R.layout.fragment_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    itemFinalPrice = view.findViewById(R.id.total_price)
    errorView = view.findViewById(R.id.error_message)
    errorMessage = view.findViewById(R.id.activity_iab_error_message)

    button.setOnClickListener { v ->
      activityView?.navigateToAdyenAuthorization(presenter.isBds, fiatValue.currency,
          paymentType)
    }
    presenter.present(extras!!.getString(TRANSACTION_DATA),
        (extras!!.getSerializable(TRANSACTION_AMOUNT) as BigDecimal).toDouble(),
        extras!!.getString(TRANSACTION_CURRENCY))

    showLoading()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    compositeDisposable.dispose()

    presenter.stop()
  }

  override fun onDetach() {
    super.onDetach()
    activityView = null
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    activityView = context
  }

  override fun setup(response: FiatValue) {
    val formatter = Formatter()
    val valueText = formatter.format(Locale.getDefault(), "%(,.2f",
        extras!!.getSerializable(TRANSACTION_AMOUNT) as BigDecimal)
        .toString() + " APPC"
    val decimalFormat = DecimalFormat("0.00")
    val currency = mapCurrencyCodeToSymbol(response.currency)
    val priceText = currency + decimalFormat.format(response.amount)

    main_value.setText(priceText)
    converted_value.text = valueText
    fiatValue = response
    button.performClick()
    val buyButtonText = R.string.action_buy
    button.text = resources.getString(buyButtonText)

    setupSubject.onNext(true)
    hideLoading()
  }

  override fun showError() {
    loading.visibility = View.GONE
    errorView.visibility = View.VISIBLE
    errorMessage.setText(R.string.activity_iab_error_message)
  }

  override fun close() {
    activityView!!.close()
  }

  override fun hideLoading() {
    loading.visibility = View.GONE
  }

  override fun showLoading() {
    loading.visibility = View.VISIBLE
  }

  override fun setupUiCompleted(): Observable<Boolean> {
    return setupSubject
  }

  @Throws(IOException::class)
  override fun finish(purchase: Purchase) {
    close()
  }

  fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return Currency.getInstance(currencyCode)
        .symbol
  }

  companion object {
    fun newInstance(extras: Bundle, isBds: Boolean,
                    paymentType: PaymentType): TopUpPaymentFragment {
      val fragment = TopUpPaymentFragment()
      val bundle = Bundle()
      bundle.putBundle("extras", extras)
      bundle.putBoolean("isBds", isBds)
      bundle.putString("paymentType", paymentType.name)
      fragment.arguments = bundle
      return fragment
    }

    @Throws(IOException::class)
    fun serializeJson(purchase: Purchase): String {
      val objectMapper = ObjectMapper()
      val developerPurchase = objectMapper.readValue(Gson().toJson(
          purchase.signature
              .message), DeveloperPurchase::class.java)
      return objectMapper.writeValueAsString(developerPurchase)
    }
  }
}
