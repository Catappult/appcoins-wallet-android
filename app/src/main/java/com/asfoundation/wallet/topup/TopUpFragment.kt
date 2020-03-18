package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpData.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodAdapter
import com.asfoundation.wallet.ui.iab.FiatValue
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_top_up.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import rx.functions.Action1
import java.math.BigDecimal
import javax.inject.Inject


class TopUpFragment : DaggerFragment(), TopUpFragmentView {

  @Inject
  lateinit var interactor: TopUpInteractor

  private lateinit var adapter: TopUpPaymentMethodAdapter
  private lateinit var presenter: TopUpFragmentPresenter
  private lateinit var paymentMethodClick: PublishRelay<String>
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var paymentMethods: List<PaymentMethodData>
  private lateinit var topUpAdapter: TopUpAdapter
  private lateinit var keyboardEvents: PublishSubject<Boolean>
  private var valueSubject: PublishSubject<FiatValue>? = null
  private var topUpActivityView: TopUpActivityView? = null
  private var selectedCurrency = FIAT_CURRENCY
  private var switchingCurrency = false
  private var bonusMessageValue: String = ""
  private var localCurrency = LocalCurrency()

  companion object {
    private const val PARAM_APP_PACKAGE = "APP_PACKAGE"
    private const val APPC_C_SYMBOL = "APPC-C"

    private const val SELECTED_CURRENCY_PARAM = "SELECTED_CURRENCY"
    private const val LOCAL_CURRENCY_PARAM = "LOCAL_CURRENCY"


    @JvmStatic
    fun newInstance(packageName: String): TopUpFragment {
      val bundle = Bundle().apply {
        putString(PARAM_APP_PACKAGE, packageName)
      }
      return TopUpFragment().apply {
        arguments = bundle
      }
    }
  }

  private val listener = ViewTreeObserver.OnGlobalLayoutListener {
    val fragmentView = this.view
    val appBarHeight = getAppBarHeight()
    fragmentView?.let {
      val heightDiff: Int = it.rootView.height - it.height - appBarHeight
      if (heightDiff > 150) {
        keyboardEvents.onNext(true)
      } else {
        keyboardEvents.onNext(false)
      }
    }
  }

  private val appPackage: String by lazy {
    if (arguments!!.containsKey(PARAM_APP_PACKAGE)) {
      arguments!!.getString(PARAM_APP_PACKAGE)
    } else {
      throw IllegalArgumentException("application package name data not found")
    }
  }

  override fun onDetach() {
    super.onDetach()
    topUpActivityView = null
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is TopUpActivityView) { "TopUp fragment must be attached to TopUp activity" }
    topUpActivityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentMethodClick = PublishRelay.create()
    valueSubject = PublishSubject.create()
    keyboardEvents = PublishSubject.create()
    presenter =
        TopUpFragmentPresenter(this, topUpActivityView, interactor, AndroidSchedulers.mainThread(),
            Schedulers.io())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    hideKeyboard()
    if (savedInstanceState?.containsKey(SELECTED_CURRENCY_PARAM) == true) {
      selectedCurrency = savedInstanceState.getString(SELECTED_CURRENCY_PARAM) ?: FIAT_CURRENCY
      localCurrency = savedInstanceState.getSerializable(LOCAL_CURRENCY_PARAM) as LocalCurrency
    }
    topUpActivityView?.showToolbar()
    presenter.present(appPackage)


    topUpAdapter = TopUpAdapter(Action1 { valueSubject?.onNext(it) })

    rv_default_values.apply {
      adapter = topUpAdapter
      addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))
    }

    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(SELECTED_CURRENCY_PARAM, selectedCurrency)
    outState.putSerializable(LOCAL_CURRENCY_PARAM, localCurrency)
  }

  override fun setupUiElements(paymentMethods: List<PaymentMethodData>,
                               localCurrency: LocalCurrency) {
    hideNoNetwork()
    if (isLocalCurrencyValid(localCurrency)) {
      this@TopUpFragment.localCurrency = localCurrency
      setupCurrencyData(selectedCurrency, localCurrency.code, DEFAULT_VALUE,
          APPC_C_SYMBOL, DEFAULT_VALUE)
    }
    this@TopUpFragment.paymentMethods = paymentMethods
    hideKeyboard()
    main_value.isEnabled = true
    main_value.setMinTextSize(
        resources.getDimensionPixelSize(R.dimen.topup_main_value_min_size)
            .toFloat())
    adapter = TopUpPaymentMethodAdapter(paymentMethods, paymentMethodClick)

    payment_methods.adapter = adapter
    payment_methods.layoutManager = LinearLayoutManager(context)
    payment_methods.visibility = View.VISIBLE
    swap_value_button.isEnabled = true
    swap_value_button.visibility = View.VISIBLE
    swap_value_label.visibility = View.VISIBLE
  }

  override fun setValuesAdapter(values: List<FiatValue>) {
    topUpAdapter.submitList(values)
  }

  override fun showValuesAdapter() {
    if (rv_default_values.visibility == View.GONE) {
      rv_default_values.visibility = View.VISIBLE
      bottom_separator.visibility = View.VISIBLE
    }
  }

  override fun hideValuesAdapter() {
    if (rv_default_values.visibility == View.VISIBLE) {
      rv_default_values.visibility = View.GONE
      bottom_separator.visibility = View.GONE
    }
  }

  override fun getKeyboardEvents(): Observable<Boolean> {
    return keyboardEvents
  }

  override fun onDestroy() {
    view?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
    presenter.stop()
    super.onDestroy()
  }

  override fun getChangeCurrencyClick(): Observable<Any> {
    return RxView.clicks(swap_value_button)
  }

  override fun disableSwapCurrencyButton() {
    swap_value_button.isEnabled = false
  }

  override fun enableSwapCurrencyButton() {
    swap_value_button.isEnabled = true
  }

  override fun getValuesClicks() = valueSubject!!

  override fun getEditTextChanges(): Observable<TopUpData> {
    return RxTextView.afterTextChangeEvents(main_value)
        .filter { !switchingCurrency }
        .map {
          TopUpData(getCurrencyData(), selectedCurrency, getSelectedPaymentMethod())
        }
  }

  override fun getPaymentMethodClick(): Observable<String> {
    return paymentMethodClick
  }

  override fun getNextClick(): Observable<TopUpData> {
    return RxView.clicks(button)
        .map {
          TopUpData(getCurrencyData(), selectedCurrency, getSelectedPaymentMethod(),
              bonusMessageValue)
        }
  }

  override fun setNextButtonState(enabled: Boolean) {
    button.isEnabled = enabled
  }

  override fun paymentMethodsFocusRequest() {
    hideKeyboard()
    payment_methods.requestFocus()
  }

  override fun showLoading() {
    credit_card_info_container.visibility = View.GONE
    payment_methods.visibility = View.INVISIBLE
    bonus_layout.visibility = View.GONE
    bonus_msg.visibility = View.GONE
    loading.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    credit_card_info_container.visibility = View.VISIBLE
    payment_methods.visibility = View.VISIBLE
    bonus_layout.visibility = View.VISIBLE
    bonus_msg.visibility = View.VISIBLE
    loading.visibility = View.INVISIBLE
  }

  override fun showPaymentDetailsForm() {
    payment_methods.visibility = View.GONE
    loading.visibility = View.GONE
    credit_card_info_container.visibility = View.VISIBLE
    bonus_layout.visibility = View.GONE
    bonus_msg.visibility = View.GONE
  }

  override fun showPaymentMethods() {
    credit_card_info_container.visibility = View.GONE
    loading.visibility = View.GONE
    payment_methods.visibility = View.VISIBLE
  }

  override fun rotateChangeCurrencyButton() {
    val rotateAnimation = RotateAnimation(
        0f,
        180f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f)
    rotateAnimation.duration = 250
    rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
    swap_value_button.startAnimation(rotateAnimation)
  }

  override fun switchCurrencyData() {
    val currencyData = getCurrencyData()
    selectedCurrency =
        if (selectedCurrency == APPC_C_CURRENCY) FIAT_CURRENCY else APPC_C_CURRENCY
    // We just have to switch the current information being shown
    setupCurrencyData(selectedCurrency, currencyData.fiatCurrencyCode, currencyData.fiatValue,
        currencyData.appcCode, currencyData.appcValue)
  }

  override fun setConversionValue(topUpData: TopUpData) {
    if (topUpData.selectedCurrency == selectedCurrency) {
      when (selectedCurrency) {
        FIAT_CURRENCY -> {
          converted_value.text = "${topUpData.currency.appcValue} ${topUpData.currency.appcSymbol}"
        }
        APPC_C_CURRENCY -> {
          converted_value.text =
              "${topUpData.currency.fiatValue} ${topUpData.currency.fiatCurrencyCode}"
        }
      }
    } else {
      when (selectedCurrency) {
        FIAT_CURRENCY -> {
          if (topUpData.currency.fiatValue != DEFAULT_VALUE) main_value.setText(
              topUpData.currency.fiatValue) else main_value.setText("")
        }
        APPC_C_CURRENCY -> {
          if (topUpData.currency.appcValue != DEFAULT_VALUE) main_value.setText(
              topUpData.currency.appcValue) else main_value.setText("")
        }
      }
    }
  }

  override fun toggleSwitchCurrencyOn() {
    switchingCurrency = true
  }

  override fun toggleSwitchCurrencyOff() {
    switchingCurrency = false
  }

  override fun hideBonus() {
    bonus_layout.visibility = View.INVISIBLE
    bonus_msg.visibility = View.INVISIBLE
  }

  override fun showBonus(bonus: BigDecimal, currency: String) {
    buildBonusString(bonus, currency)
    bonus_layout.visibility = View.VISIBLE
    bonus_msg.visibility = View.VISIBLE
  }

  override fun showMaxValueWarning(value: String) {
    value_warning_text.text = getString(R.string.topup_maximum_value, value)
    value_warning_icon.visibility = View.VISIBLE
    value_warning_text.visibility = View.VISIBLE
  }

  override fun showMinValueWarning(value: String) {
    value_warning_text.text = getString(R.string.topup_minimum_value, value)
    value_warning_icon.visibility = View.VISIBLE
    value_warning_text.visibility = View.VISIBLE
  }

  override fun hideValueInputWarning() {
    value_warning_icon.visibility = View.INVISIBLE
    value_warning_text.visibility = View.INVISIBLE
  }

  override fun changeMainValueColor(isValid: Boolean) {
    if (isValid) {
      main_value.setTextColor(ContextCompat.getColor(context!!, R.color.black))
    } else {
      main_value.setTextColor(ContextCompat.getColor(context!!, R.color.color_grey_9e))
    }
  }

  override fun changeMainValueText(value: String) {
    main_value.setText(value)
    main_value.setSelection(value.length)
  }

  override fun getSelectedCurrency(): String {
    return selectedCurrency
  }

  override fun initialInputSetup(preselectedChip: Int, preselectedChipValue: BigDecimal) {
    hideKeyboard()
    if (preselectedChipValue.toDouble() > 0) {
      changeMainValueText(preselectedChipValue.toString())
    }
  }

  override fun showNoNetworkError() {
    no_network.visibility = View.VISIBLE
    retry_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
    top_up_container.visibility = View.GONE
    rv_default_values.visibility = View.GONE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun retryClick(): Observable<Any> {
    return RxView.clicks(retry_button)
  }

  private fun hideNoNetwork() {
    no_network.visibility = View.GONE
    retry_button.visibility = View.GONE
    retry_animation.visibility = View.GONE
    top_up_container.visibility = View.VISIBLE
  }

  private fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
  }

  private fun buildBonusString(bonus: BigDecimal, bonusCurrency: String) {
    var scaledBonus = bonus.stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_FLOOR)
    var currency = bonusCurrency
    if (scaledBonus < BigDecimal(0.01)) {
      currency = "~$currency"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))

    bonusMessageValue = currency + scaledBonus.toPlainString()
    bonus_layout.bonus_header_1.text = getString(R.string.topup_bonus_header_part_1)
    bonus_layout.bonus_value.text = getString(R.string.topup_bonus_header_part_2,
        currency + scaledBonus.toPlainString())
  }

  private fun setupCurrencyData(selectedCurrency: String, fiatCode: String, fiatValue: String,
                                appcCode: String, appcValue: String) {

    when (selectedCurrency) {
      FIAT_CURRENCY -> {
        setCurrencyInfo(fiatCode, fiatValue,
            "$appcValue $appcCode", appcCode)
      }
      APPC_C_CURRENCY -> {
        setCurrencyInfo(appcCode, appcValue,
            "$fiatValue $fiatCode", fiatCode)
      }
    }
  }

  private fun setCurrencyInfo(mainCode: String, mainValue: String,
                              conversionValue: String, conversionCode: String) {
    main_currency_code.text = mainCode
    if (mainValue != DEFAULT_VALUE) {
      main_value.setText(mainValue)
      main_value.setSelection(main_value.text!!.length)
    }
    swap_value_label.text = conversionCode
    converted_value.text = conversionValue
  }

  private fun getSelectedPaymentMethod(): PaymentType {
    return if (payment_methods.adapter != null) {
      val data = (payment_methods.adapter as TopUpPaymentMethodAdapter).getSelectedItemData()
      if (PaymentType.PAYPAL.subTypes.contains(data.id)) {
        PaymentType.PAYPAL
      } else {
        PaymentType.CARD
      }
    } else {
      PaymentType.CARD
    }
  }

  private fun getCurrencyData(): CurrencyData {
    return if (selectedCurrency == FIAT_CURRENCY) {
      val appcValue = converted_value.text.toString()
          .replace(APPC_C_SYMBOL, "")
          .replace(" ", "")
      val localCurrencyValue =
          if (main_value.text.toString()
                  .isEmpty()) DEFAULT_VALUE else main_value.text.toString()
      CurrencyData(localCurrency.code, localCurrency.symbol, localCurrencyValue,
          APPC_C_SYMBOL, APPC_C_SYMBOL, appcValue)
    } else {
      val localCurrencyValue = converted_value.text.toString()
          .replace(localCurrency.code, "")
          .replace(" ", "")
      val appcValue =
          if (main_value.text.toString()
                  .isEmpty()) DEFAULT_VALUE else main_value.text.toString()
      CurrencyData(localCurrency.code, localCurrency.symbol, localCurrencyValue,
          APPC_C_SYMBOL, APPC_C_SYMBOL, appcValue)
    }
  }

  private fun isLocalCurrencyValid(localCurrency: LocalCurrency): Boolean {
    return localCurrency.symbol != "" && localCurrency.code != ""
  }

  private fun getAppBarHeight(): Int {
    if (context == null) {
      return 0
    }
    return with(TypedValue().also {
      context!!.theme.resolveAttribute(android.R.attr.actionBarSize, it, true)
    }) {
      TypedValue.complexToDimensionPixelSize(this.data, resources.displayMetrics)
    }
  }
}
