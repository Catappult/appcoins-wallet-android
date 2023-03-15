package com.asfoundation.wallet.topup

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpData.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodsAdapter
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.asfoundation.wallet.util.convertDpToPx
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_top_up.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import rx.functions.Action1
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class TopUpFragment : BasePageViewFragment(), TopUpFragmentView {

  @Inject
  lateinit var interactor: TopUpInteractor

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private lateinit var adapter: TopUpPaymentMethodsAdapter
  private lateinit var presenter: TopUpFragmentPresenter
  private lateinit var paymentMethodClick: PublishRelay<String>
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var paymentMethods: List<PaymentMethod>
  private lateinit var topUpAdapter: TopUpAdapter
  private lateinit var keyboardEvents: PublishSubject<Boolean>
  private var valueSubject: PublishSubject<FiatValue>? = null
  private var topUpActivityView: TopUpActivityView? = null
  private var selectedCurrency = FIAT_CURRENCY
  private var switchingCurrency = false
  private var bonusValue = BigDecimal.ZERO
  private var localCurrency = LocalCurrency()
  private var selectedPaymentMethodId: String? = null

  companion object {
    private const val PARAM_APP_PACKAGE = "APP_PACKAGE"
    private const val APPC_C_SYMBOL = "APPC-C"

    private const val SELECTED_VALUE_PARAM = "SELECTED_VALUE"
    private const val SELECTED_PAYMENT_METHOD_PARAM = "SELECTED_PAYMENT_METHOD"
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

      val threshold = 150.convertDpToPx(resources)

      keyboardEvents.onNext(heightDiff > threshold)
    }
  }

  private val appPackage: String by lazy {
    if (requireArguments().containsKey(PARAM_APP_PACKAGE)) {
      requireArguments().getString(PARAM_APP_PACKAGE)!!
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
    presenter = TopUpFragmentPresenter(this, topUpActivityView, interactor,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), topUpAnalytics,
        formatter, savedInstanceState?.getString(SELECTED_VALUE_PARAM))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    savedInstanceState?.let {
      if (savedInstanceState.containsKey(SELECTED_CURRENCY_PARAM)) {
        selectedCurrency = savedInstanceState.getString(SELECTED_CURRENCY_PARAM) ?: FIAT_CURRENCY
        localCurrency = savedInstanceState.getSerializable(LOCAL_CURRENCY_PARAM) as LocalCurrency
      }
      selectedPaymentMethodId = it.getString(SELECTED_PAYMENT_METHOD_PARAM)
    }
    presenter.present(appPackage, savedInstanceState)

    topUpAdapter = TopUpAdapter(Action1 { valueSubject?.onNext(it) })
    rv_default_values.apply {
      adapter = topUpAdapter
    }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(SELECTED_VALUE_PARAM, main_value.text.toString())
    if (::adapter.isInitialized) {
      outState.putString(SELECTED_PAYMENT_METHOD_PARAM, adapter.getSelectedItemData().id)
    }
    outState.putString(SELECTED_CURRENCY_PARAM, selectedCurrency)
    outState.putSerializable(LOCAL_CURRENCY_PARAM, localCurrency)
    presenter.onSavedInstance(outState)
  }

  override fun setupPaymentMethods(paymentMethods: List<PaymentMethod>) {
    this@TopUpFragment.paymentMethods = paymentMethods
    adapter = TopUpPaymentMethodsAdapter(paymentMethods, paymentMethodClick)
    selectPaymentMethod(paymentMethods)

    payment_methods.adapter = adapter

    handlePaymentListMaxHeight(paymentMethods.size)

    payments_skeleton.visibility = View.GONE
    payment_methods.visibility = View.VISIBLE
  }

  private fun handlePaymentListMaxHeight(listSize: Int) {
    if (listSize > 2) {
      val orientation = resources.configuration.orientation
      val params: LayoutParams = payment_methods.layoutParams as LayoutParams
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        params.height = 164.convertDpToPx(resources)
      }
      if (orientation == Configuration.ORIENTATION_PORTRAIT && paymentMethods.size > 3) {
        params.height = 228.convertDpToPx(resources)
      }
      payment_methods.layoutParams = params
    }
  }

  private fun selectPaymentMethod(paymentMethods: List<PaymentMethod>) {
    var selected = false
    if (selectedPaymentMethodId != null) {
      for (i in paymentMethods.indices) {
        if (paymentMethods[i].id == selectedPaymentMethodId && paymentMethods[i].isEnabled) {
          selectedPaymentMethodId = paymentMethods[i].id
          adapter.setSelectedItem(i)
          selected = true
        }
      }
    }
    if (!selected) adapter.setSelectedItem(0)
  }

  override fun setupCurrency(localCurrency: LocalCurrency) {
    hideErrorViews()
    if (isLocalCurrencyValid(localCurrency)) {
      this@TopUpFragment.localCurrency = localCurrency
      setupCurrencyData(selectedCurrency, localCurrency.code, DEFAULT_VALUE, APPC_C_SYMBOL,
          DEFAULT_VALUE)
    }
    main_value.isEnabled = true
    main_value.setMinTextSize(resources.getDimensionPixelSize(R.dimen.topup_main_value_min_size)
        .toFloat())
    main_value.setOnEditorActionListener { _, actionId, _ ->
      if (EditorInfo.IME_ACTION_NEXT == actionId) {
        hideKeyboard()
        button.performClick()
      }
      true
    }
    top_separator_topup.visibility = View.VISIBLE
    bot_separator.visibility = View.VISIBLE
    swap_value_button.isEnabled = true
    swap_value_button.visibility = View.VISIBLE
    swap_value_label.visibility = View.VISIBLE
    //added since this fragment continues active after navigating to the payment fragment
    if (fragmentManager?.backStackEntryCount == 0) focusAndShowKeyboard(main_value)

  }

  private fun focusAndShowKeyboard(view: EditText) {
    view.post {
      view.requestFocus()
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
  }

  override fun setDefaultAmountValue(amount: String) {
    setupCurrencyData(selectedCurrency, localCurrency.code, amount, APPC_C_SYMBOL, DEFAULT_VALUE)
  }

  override fun setValuesAdapter(values: List<FiatValue>) {
    val addMargin = values.size <= getTopUpValuesSpanCount()
    rv_default_values.addItemDecoration(
        DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))
    rv_default_values.addItemDecoration(TopUpItemDecorator(values.size, addMargin))

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

  override fun onPause() {
    hideKeyboard()
    super.onPause()
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
          TopUpData(getCurrencyData(), selectedCurrency, getSelectedPaymentMethod(), bonusValue)
        }
  }

  override fun setNextButtonState(enabled: Boolean) {
    button.isEnabled = enabled
  }

  override fun paymentMethodsFocusRequest() {
    hideKeyboard()
    payment_methods.requestFocus()
    selectedPaymentMethodId = adapter.getSelectedItemData().id
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
    if (topUpData.selectedCurrencyType == selectedCurrency) {
      when (selectedCurrency) {
        FIAT_CURRENCY -> {
          converted_value.text = "${topUpData.currency.appcValue} ${WalletCurrency.CREDITS.symbol}"
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

  override fun hideBonusAndSkeletons() {
    hideBonus()
    bonus_layout_skeleton.visibility = View.GONE
    bonus_msg_skeleton.visibility = View.GONE
  }

  override fun removeBonus() {
    bonus_layout.visibility = View.GONE
    bonus_msg.visibility = View.GONE
    bonus_layout_skeleton.visibility = View.GONE
    bonus_msg_skeleton.visibility = View.GONE
  }

  override fun showBonus(bonus: BigDecimal, currency: String) {
    buildBonusString(bonus, currency)
    showBonus()
  }

  private fun showBonus() {
    bonus_layout_skeleton.visibility = View.GONE
    bonus_msg_skeleton.visibility = View.GONE
    bonus_msg.visibility = View.VISIBLE
    bonus_layout.visibility = View.VISIBLE
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
      main_value.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_blue_transparent_90))
    } else {
      main_value.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_medium_grey))
    }
  }

  override fun changeMainValueText(value: String) {
    main_value.setText(value)
    main_value.setSelection(value.length)
  }

  override fun getSelectedCurrency(): String {
    return selectedCurrency
  }

  override fun showNoNetworkError() {
    hideKeyboard()
    retry_animation.visibility = View.GONE
    top_up_container.visibility = View.GONE
    rv_default_values.visibility = View.GONE
    no_network.visibility = View.VISIBLE
    retry_button.visibility = View.VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun showSkeletons() {
    payments_skeleton.visibility = View.VISIBLE
    bonus_layout_skeleton.visibility = View.VISIBLE
    bonus_msg_skeleton.visibility = View.VISIBLE
  }

  override fun showBonusSkeletons() {
    bonus_msg.visibility = View.INVISIBLE
    bonus_layout.visibility = View.INVISIBLE
    bonus_layout_skeleton.visibility = View.VISIBLE
    bonus_msg_skeleton.visibility = View.VISIBLE
  }

  override fun hidePaymentMethods() {
    payments_skeleton.visibility = View.VISIBLE
    payment_methods.visibility = View.GONE
  }

  private fun hideErrorViews() {
    no_network.visibility = View.GONE
    retry_button.visibility = View.GONE
    retry_animation.visibility = View.GONE
    top_up_container.visibility = View.VISIBLE
  }

  override fun showNoMethodsError() {
    hideKeyboard()
    retry_animation.visibility = View.GONE
    top_up_container.visibility = View.GONE
    rv_default_values.visibility = View.GONE
    error_topup.startAnimation(AnimationUtils.loadAnimation(context,R.anim.pop_in_animation))
    error_topup.visibility = View.VISIBLE
  }

  private fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
  }

  private fun buildBonusString(bonus: BigDecimal, bonusCurrency: String) {
    val scaledBonus = bonus.max(BigDecimal("0.01"))
    val currency = "~$bonusCurrency".takeIf { bonus < BigDecimal("0.01") } ?: bonusCurrency
    bonusValue = scaledBonus
    bonus_layout.bonus_header_1.text = getString(R.string.topup_bonus_header_part_1)
    bonus_layout.bonus_value.text = getString(R.string.topup_bonus_header_part_2,
        currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT))
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

  private fun getSelectedPaymentMethod(): PaymentTypeInfo? {
    return if (payment_methods.adapter != null) {
      val data = (payment_methods.adapter as TopUpPaymentMethodsAdapter).getSelectedItemData()
      when {
        PaymentType.PAYPAL.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.PAYPAL, data.id, data.label, data.iconUrl)
        PaymentType.PAYPALV2.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.PAYPALV2, data.id, data.label, data.iconUrl)
        PaymentType.CARD.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.CARD, data.id, data.label, data.iconUrl)
        else -> PaymentTypeInfo(PaymentType.LOCAL_PAYMENTS, data.id, data.label,
            data.iconUrl, data.async)
      }
    } else {
      null
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
      requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, it, true)
    }) {
      TypedValue.complexToDimensionPixelSize(this.data, resources.displayMetrics)
    }
  }

  private fun getTopUpValuesSpanCount(): Int {
    val screenWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
            fragmentContainer.measuredWidth.toFloat(),
            requireContext().resources
                .displayMetrics)
            .toInt()
    val viewWidth = 80.convertDpToPx(resources)

    return screenWidth / viewWidth
  }
}
