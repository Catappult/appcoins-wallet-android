package com.asfoundation.wallet.topup

import android.content.Context
import android.content.pm.ActivityInfo
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetShowRefundDisclaimerCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.sharedpreferences.CardPaymentDataSource
import com.appcoins.wallet.ui.common.convertDpToPx
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentTopUpBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.paypal.usecases.IsPaypalAgreementCreatedUseCase
import com.asfoundation.wallet.billing.paypal.usecases.RemovePaypalBillingAgreementUseCase
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.asfoundation.wallet.topup.TopUpData.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodsAdapter
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import rx.functions.Action1
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class TopUpFragment : BasePageViewFragment(), TopUpFragmentView {

  @Inject
  lateinit var interactor: TopUpInteractor

  @Inject
  lateinit var getWalletInfoUseCase: GetWalletInfoUseCase

  @Inject
  lateinit var removePaypalBillingAgreementUseCase: RemovePaypalBillingAgreementUseCase

  @Inject
  lateinit var isPaypalAgreementCreatedUseCase: IsPaypalAgreementCreatedUseCase

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var getCachedCurrencyUseCase: GetCachedCurrencyUseCase

  @Inject
  lateinit var getStoredCardsUseCase: GetStoredCardsUseCase

  @Inject
  lateinit var cardPaymentDataSource: CardPaymentDataSource

  @Inject
  lateinit var getCurrentWalletUseCase: GetCurrentWalletUseCase

  @Inject
  lateinit var getCachedShowRefundDisclaimerUseCase: GetCachedShowRefundDisclaimerUseCase

  @Inject
  lateinit var getShowRefundDisclaimerCodeUseCase: GetShowRefundDisclaimerCodeUseCase

  @Inject
  lateinit var setCachedShowRefundDisclaimerUseCase: SetCachedShowRefundDisclaimerUseCase

  private lateinit var adapter: TopUpPaymentMethodsAdapter
  private lateinit var presenter: TopUpFragmentPresenter
  private lateinit var paymentMethodClick: PublishRelay<PaymentMethod>
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var paymentMethods: List<PaymentMethod>
  private var cardsList: List<StoredCard> = listOf()
  private lateinit var topUpAdapter: TopUpAdapter
  private lateinit var keyboardEvents: PublishSubject<Boolean>
  private var valueSubject: PublishSubject<FiatValue>? = null
  private var topUpActivityView: TopUpActivityView? = null
  private var selectedCurrency = FIAT_CURRENCY
  private var switchingCurrency = false
  private var bonusValue = BigDecimal.ZERO
  private var localCurrency = LocalCurrency()
  private var selectedPaymentMethodId: String? = null
  private var showBottomSheet = mutableStateOf(false)

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

  private val binding by viewBinding(FragmentTopUpBinding::bind)

  override fun onDetach() {
    super.onDetach()
    topUpActivityView = null
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
      context is TopUpActivityView
    ) { "TopUp fragment must be attached to TopUp activity" }
    topUpActivityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentMethodClick = PublishRelay.create()
    valueSubject = PublishSubject.create()
    keyboardEvents = PublishSubject.create()
    presenter = TopUpFragmentPresenter(
      view = this,
      activity = topUpActivityView,
      interactor = interactor,
      getWalletInfoUseCase = getWalletInfoUseCase,
      removePaypalBillingAgreementUseCase = removePaypalBillingAgreementUseCase,
      isPaypalAgreementCreatedUseCase = isPaypalAgreementCreatedUseCase,
      viewScheduler = AndroidSchedulers.mainThread(),
      networkScheduler = Schedulers.io(),
      disposables = CompositeDisposable(),
      topUpAnalytics = topUpAnalytics,
      formatter = formatter,
      selectedValue = savedInstanceState?.getString(SELECTED_VALUE_PARAM),
      logger = logger,
      networkThread = Schedulers.io(),
      getCachedCurrencyUseCase = getCachedCurrencyUseCase,
      getStoredCardsUseCase = getStoredCardsUseCase,
      cardPaymentDataSource = cardPaymentDataSource,
      getCurrentWalletUseCase = getCurrentWalletUseCase,
      getShowRefundDisclaimerCodeUseCase = getShowRefundDisclaimerCodeUseCase,
      setCachedShowRefundDisclaimerUseCase = setCachedShowRefundDisclaimerUseCase

    )
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    fragmentContainer = container!!
    return FragmentTopUpBinding.inflate(inflater).root
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
    binding.rvDefaultValues.apply {
      adapter = topUpAdapter
    }
    view.findViewById<ComposeView>(R.id.composeView).apply {
      setContent {
        if (showBottomSheet.value) {
          topUpAnalytics.openChangeCardBottomSheet()
          ShowCardListBottomSheet()
        }
      }
    }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(SELECTED_VALUE_PARAM, binding.mainValue.text.toString())
    if (::adapter.isInitialized) {
      outState.putString(SELECTED_PAYMENT_METHOD_PARAM, adapter.getSelectedItemData().id)
    }
    outState.putString(SELECTED_CURRENCY_PARAM, selectedCurrency)
    outState.putSerializable(LOCAL_CURRENCY_PARAM, localCurrency)
    presenter.onSavedInstance(outState)
  }

  override fun setupPaymentMethods(
    paymentMethods: List<PaymentMethod>,
    cardsList: List<StoredCard>
  ) {
    this@TopUpFragment.paymentMethods = paymentMethods
    if (this.cardsList.isNullOrEmpty() && cardsList.isNotEmpty()) {
      if (presenter.storedCardID.isNullOrEmpty()) {
        cardsList.first().isSelectedCard = true
        cardsList.first().recurringReference?.let {
          presenter.setCardIdSharedPreferences(it)
        }
      }
      presenter.hasStoredCard = true
      setBuyButton()
      this@TopUpFragment.cardsList = cardsList
    }

    adapter = TopUpPaymentMethodsAdapter(
      paymentMethods = paymentMethods,
      paymentMethodClick = paymentMethodClick,
      logoutCallback = {
        presenter.removePaypalBillingAgreement()
        presenter.showPayPalLogout.onNext(false)
        setNextButton()
        showAsLoading()
      },
      disposables = presenter.disposables,
      showPayPalLogout = presenter.showPayPalLogout,
      cardsList = this.cardsList,
      onChangeCardCallback = { showBottomSheet.value = true }
    )
    selectPaymentMethod(paymentMethods)

    binding.paymentMethods.adapter = adapter


    binding.paymentsSkeleton.visibility = View.GONE
    binding.paymentMethods.visibility = View.VISIBLE
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

  override fun setupCurrency(currency: LocalCurrency) {
    hideErrorViews()
    if (isLocalCurrencyValid(currency)) {
      localCurrency = currency
      setupCurrencyData(
        selectedCurrency, currency.code, DEFAULT_VALUE, APPC_C_SYMBOL,
        DEFAULT_VALUE
      )
    }
    binding.mainCurrencyCode.text = currency.code
    binding.mainValue.isEnabled = true
    binding.mainValue.setMinTextSize(
      resources.getDimensionPixelSize(R.dimen.topup_main_value_min_size)
        .toFloat()
    )
    binding.mainValue.setOnEditorActionListener { _, actionId, _ ->
      if (EditorInfo.IME_ACTION_NEXT == actionId) {
        hideKeyboard()
        binding.button.performClick()
      }
      true
    }
  }

  override fun showAsLoading() {
    setNextButtonState(enabled = false)
    binding.paymentsSkeleton.visibility = View.VISIBLE
    binding.paymentMethods.visibility = View.INVISIBLE
  }

  override fun hideLoading() {
    setNextButtonState(enabled = true)
    binding.paymentsSkeleton.visibility = View.GONE
    binding.paymentMethods.visibility = View.VISIBLE
  }

  override fun setDefaultAmountValue(amount: String) {
    setupCurrencyData(selectedCurrency, localCurrency.code, amount, APPC_C_SYMBOL, DEFAULT_VALUE)
  }

  override fun setValuesAdapter(values: List<FiatValue>) {
    val addMargin = values.size <= getTopUpValuesSpanCount()
    binding.rvDefaultValues.addItemDecoration(
      DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
    )
    binding.rvDefaultValues.addItemDecoration(TopUpItemDecorator(values.size, addMargin))

    topUpAdapter.submitList(values)
  }

  override fun showValuesAdapter() {
    if (binding.rvDefaultValues.visibility == View.GONE) {
      binding.rvDefaultValues.visibility = View.VISIBLE
      binding.bottomSeparator.visibility = View.VISIBLE
      binding.tvRefundDisclaimer.visibility = View.GONE
      binding.button.visibility = View.GONE
    }
  }

  override fun hideValuesAdapter() {
    if (binding.rvDefaultValues.visibility == View.VISIBLE) {
      binding.rvDefaultValues.visibility = View.GONE
      binding.bottomSeparator.visibility = View.GONE
      binding.tvRefundDisclaimer.visibility =
        if (getCachedShowRefundDisclaimerUseCase()) View.VISIBLE else View.GONE
      binding.button.visibility = View.VISIBLE
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

  override fun getValuesClicks() = valueSubject!!

  override fun getEditTextChanges(): Observable<TopUpData> {
    return RxTextView.afterTextChangeEvents(binding.mainValue)
      .filter { !switchingCurrency }
      .map {
        TopUpData(getCurrencyData(), selectedCurrency, getSelectedPaymentMethod())
      }
  }

  override fun getPaymentMethodClick(): Observable<PaymentMethod> {
    return paymentMethodClick
  }

  override fun getNextClick(): Observable<TopUpData> {
    return RxView.clicks(binding.button)
      .map {
        TopUpData(getCurrencyData(), selectedCurrency, getSelectedPaymentMethod(), bonusValue)
      }
  }

  override fun setNextButtonState(enabled: Boolean) {
    binding.button.isEnabled = enabled
  }

  override fun paymentMethodsFocusRequest() {
    hideKeyboard()
    binding.paymentMethods.requestFocus()
    selectedPaymentMethodId = adapter.getSelectedItemData().id
  }

  override fun getCurrentPaymentMethod(): String? = selectedPaymentMethodId

  override fun rotateChangeCurrencyButton() {
    val rotateAnimation = RotateAnimation(
      0f,
      180f,
      Animation.RELATIVE_TO_SELF,
      0.5f,
      Animation.RELATIVE_TO_SELF,
      0.5f
    )
    rotateAnimation.duration = 250
    rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
  }

  override fun switchCurrencyData() {
    val currencyData = getCurrencyData()
    selectedCurrency =
      if (selectedCurrency == APPC_C_CURRENCY) FIAT_CURRENCY else APPC_C_CURRENCY
    // We just have to switch the current information being shown
    setupCurrencyData(
      selectedCurrency, currencyData.fiatCurrencyCode, currencyData.fiatValue,
      currencyData.appcCode, currencyData.appcValue
    )
  }

  override fun setConversionValue(topUpData: TopUpData) {
    if (topUpData.selectedCurrencyType == selectedCurrency) {
      when (selectedCurrency) {
        FIAT_CURRENCY -> {
          binding.convertedValue.text =
            "${topUpData.currency.appcValue} ${WalletCurrency.CREDITS.symbol}"
        }

        APPC_C_CURRENCY -> {
          binding.convertedValue.text =
            "${topUpData.currency.fiatValue} ${topUpData.currency.fiatCurrencyCode}"
        }
      }
    } else {
      when (selectedCurrency) {
        FIAT_CURRENCY -> {
          if (topUpData.currency.fiatValue != DEFAULT_VALUE) binding.mainValue.setText(
            topUpData.currency.fiatValue
          ) else binding.mainValue.setText("")
        }

        APPC_C_CURRENCY -> {
          if (topUpData.currency.appcValue != DEFAULT_VALUE) binding.mainValue.setText(
            topUpData.currency.appcValue
          ) else binding.mainValue.setText("")
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
    binding.bonusLayout.root.visibility = View.INVISIBLE
  }

  override fun hideBonusAndSkeletons() {
    hideBonus()
    binding.bonusLayoutSkeleton.root.visibility = View.GONE
  }

  override fun removeBonus() {
    binding.bonusLayout.root.visibility = View.GONE
    binding.bonusLayoutSkeleton.root.visibility = View.GONE
  }

  override fun showBonus(bonus: BigDecimal, currency: String) {
    buildBonusString(bonus, currency)
    showBonus()
  }

  override fun showBonus() {
    binding.bonusLayoutSkeleton.root.visibility = View.GONE
    binding.bonusLayout.root.visibility = View.VISIBLE
  }

  override fun showMaxValueWarning(value: String) {
    binding.valueWarningText.text = getString(R.string.topup_maximum_value, value)
    binding.valueWarningIcon.visibility = View.VISIBLE
    binding.valueWarningText.visibility = View.VISIBLE
  }

  override fun showMinValueWarning(value: String) {
    binding.valueWarningText.text = getString(R.string.topup_minimum_value, value)
    binding.valueWarningIcon.visibility = View.VISIBLE
    binding.valueWarningText.visibility = View.VISIBLE
  }

  override fun hideValueInputWarning() {
    binding.valueWarningIcon.visibility = View.INVISIBLE
    binding.valueWarningText.visibility = View.INVISIBLE
  }

  override fun changeMainValueColor(isValid: Boolean) {
    if (isValid) {
      binding.mainValue.setTextColor(
        ContextCompat.getColor(
          requireContext(),
          R.color.styleguide_light_grey
        )
      )
    } else {
      binding.mainValue.setTextColor(
        ContextCompat.getColor(
          requireContext(),
          R.color.styleguide_dark_grey
        )
      )
    }
  }

  override fun changeMainValueText(value: String) {
    binding.mainValue.setText(value)
    binding.mainValue.setSelection(value.length)
  }

  override fun getSelectedCurrencyType(): String {
    return selectedCurrency
  }

  override fun getSelectedCurrency(): LocalCurrency {
    return localCurrency
  }

  override fun showNoNetworkError() {
    hideKeyboard()
    binding.noNetwork.retryAnimation.visibility = View.GONE
    binding.topUpContainer.visibility = View.GONE
    binding.rvDefaultValues.visibility = View.GONE
    binding.noNetwork.root.visibility = View.VISIBLE
    binding.noNetwork.retryButton.visibility = View.VISIBLE
  }

  override fun showRetryAnimation() {
    binding.noNetwork.retryButton.visibility = View.INVISIBLE
    binding.noNetwork.retryAnimation.visibility = View.VISIBLE
  }

  override fun retryClick() = RxView.clicks(binding.noNetwork.retryButton)

  override fun showSkeletons() {
    binding.paymentsSkeleton.visibility = View.VISIBLE
    binding.bonusLayoutSkeleton.root.visibility = View.VISIBLE
  }

  override fun showBonusSkeletons() {
    binding.bonusLayout.root.visibility = View.INVISIBLE
    binding.bonusLayoutSkeleton.root.visibility = View.VISIBLE
  }

  override fun showValuesSkeletons() {
    binding.mainCurrencyCode.visibility = View.INVISIBLE
    binding.mainValue.visibility = View.INVISIBLE
    binding.mainCurrencySkeleton.root.visibility = View.VISIBLE
    binding.mainValueSkeleton.root.visibility = View.VISIBLE
    showBonusSkeletons()
  }

  override fun hideValuesSkeletons() {
    binding.mainCurrencyCode.visibility = View.VISIBLE
    binding.mainValue.visibility = View.VISIBLE
    binding.mainCurrencySkeleton.root.visibility = View.INVISIBLE
    binding.mainValueSkeleton.root.visibility = View.INVISIBLE
  }

  override fun hidePaymentMethods() {
    binding.paymentsSkeleton.visibility = View.VISIBLE
    binding.paymentMethods.visibility = View.GONE
  }

  private fun hideErrorViews() {
    binding.noNetwork.root.visibility = View.GONE
    binding.noNetwork.retryButton.visibility = View.GONE
    binding.noNetwork.retryAnimation.visibility = View.GONE
    binding.topUpContainer.visibility = View.VISIBLE
  }

  override fun showNoMethodsError() {
    hideKeyboard()
    binding.noNetwork.retryAnimation.visibility = View.GONE
    binding.topUpContainer.visibility = View.GONE
    binding.rvDefaultValues.visibility = View.GONE
    binding.errorTopup.root.startAnimation(
      AnimationUtils.loadAnimation(
        context,
        R.anim.pop_in_animation
      )
    )
    binding.errorTopup.root.visibility = View.VISIBLE
  }

  override fun setNextButton() {
    binding.button.setTextRes(R.string.action_next)
  }

  override fun setTopupButton() {
    binding.button.setTextRes(R.string.topup_button)
  }

  override fun setBuyButton() {
    binding.button.setTextRes(R.string.buy_button)
  }

  private fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
  }

  private fun buildBonusString(bonus: BigDecimal, bonusCurrency: String) {
    val scaledBonus = bonus.max(BigDecimal("0.01"))
    val currency = "~$bonusCurrency".takeIf { bonus < BigDecimal("0.01") } ?: bonusCurrency
    bonusValue = scaledBonus
    binding.bonusLayout.bonusValue.text = getString(
      R.string.topup_bonus_amount_body,
      currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    )
  }

  private fun setupCurrencyData(
    selectedCurrency: String, fiatCode: String, fiatValue: String,
    appcCode: String, appcValue: String
  ) {

    when (selectedCurrency) {
      FIAT_CURRENCY -> {
        setCurrencyInfo(
          fiatCode, fiatValue,
          "$appcValue $appcCode"
        )
      }

      APPC_C_CURRENCY -> {
        setCurrencyInfo(
          appcCode, appcValue,
          "$fiatValue $fiatCode"
        )
      }
    }
  }

  private fun setCurrencyInfo(
    mainCode: String, mainValue: String,
    conversionValue: String
  ) {
    binding.mainCurrencyCode.text = mainCode
    if (mainValue != DEFAULT_VALUE) {
      binding.mainValue.setText(mainValue)
      binding.mainValue.setSelection(binding.mainValue.text!!.length)
    }
    binding.convertedValue.text = conversionValue
  }

  private fun getSelectedPaymentMethod(): PaymentTypeInfo? {
    return if (binding.paymentMethods.adapter != null) {
      val data =
        (binding.paymentMethods.adapter as TopUpPaymentMethodsAdapter).getSelectedItemData()
      when {
        PaymentType.PAYPAL.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.PAYPAL, data.id, data.label, data.iconUrl)

        PaymentType.PAYPALV2.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.PAYPALV2, data.id, data.label, data.iconUrl)

        PaymentType.CARD.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.CARD, data.id, data.label, data.iconUrl)

        PaymentType.CHALLENGE_REWARD.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.CHALLENGE_REWARD, data.id, data.label, data.iconUrl)

        PaymentType.VKPAY.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.VKPAY, data.id, data.label, data.iconUrl)

        PaymentType.GOOGLEPAY_WEB.subTypes.contains(data.id) ->
          PaymentTypeInfo(PaymentType.GOOGLEPAY_WEB, data.id, data.label, data.iconUrl)

        else -> PaymentTypeInfo(
          PaymentType.LOCAL_PAYMENTS, data.id, data.label,
          data.iconUrl, data.async
        )
      }
    } else {
      null
    }
  }

  private fun setSelectedCard(storedCard: StoredCard?) {
    if (storedCard != null && cardsList.contains(storedCard)) {
      cardsList.find { it.isSelectedCard }?.isSelectedCard = false
      cardsList.find { it == storedCard }?.isSelectedCard = true
      storedCard.recurringReference?.let {
        presenter.setCardIdSharedPreferences(it)
      }
    }
    adapter = TopUpPaymentMethodsAdapter(
      paymentMethods = paymentMethods,
      paymentMethodClick = paymentMethodClick,
      logoutCallback = {
        presenter.removePaypalBillingAgreement()
        presenter.showPayPalLogout.onNext(false)
        setNextButton()
        showAsLoading()
      },
      disposables = presenter.disposables,
      showPayPalLogout = presenter.showPayPalLogout,
      cardsList = this.cardsList,
      onChangeCardCallback = { showBottomSheet.value = true }
    )
    binding.paymentMethods.adapter = adapter

  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun ShowCardListBottomSheet() {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showBottomSheet.value = false },
      sheetState = bottomSheetState,
      containerColor = WalletColors.styleguide_blue_secondary
    ) {
      if (cardsList.isNotEmpty()) {
        var isGotItVisible by remember { mutableStateOf(cardPaymentDataSource.isGotItVisible()) }
        CardListBottomSheet(
          onAddNewCardClick = {
            topUpAnalytics.sendChangeAndAddCardClickEvent()
            showBottomSheet.value = false
            presenter.handleNewCardActon(
              TopUpData(
                getCurrencyData(),
                selectedCurrency,
                getSelectedPaymentMethod(),
                bonusValue
              )
            )
            binding.button.performClick()
          },
          onChangeCardClick = { storedCard, _ ->
            topUpAnalytics.sendChangeAndAddCardClickEvent()
            setSelectedCard(storedCard)
            showBottomSheet.value = false
          },
          onGotItClick = {
            cardPaymentDataSource.setGotItManageCard(false)
            isGotItVisible = false
          },
          cardList = cardsList,
          isGotItVisible = isGotItVisible
        )
      }
    }
  }

  private fun getCurrencyData(): CurrencyData {
    return if (selectedCurrency == FIAT_CURRENCY) {
      val appcValue = binding.convertedValue.text.toString()
        .replace(APPC_C_SYMBOL, "")
        .replace(" ", "")
      val localCurrencyValue =
        if (binding.mainValue.text.toString()
            .isEmpty()
        ) DEFAULT_VALUE else binding.mainValue.text.toString()
      CurrencyData(
        localCurrency.code, localCurrency.symbol, localCurrencyValue,
        APPC_C_SYMBOL, APPC_C_SYMBOL, appcValue
      )
    } else {
      val localCurrencyValue = binding.convertedValue.text.toString()
        .replace(localCurrency.code, "")
        .replace(" ", "")
      val appcValue =
        if (binding.mainValue.text.toString()
            .isEmpty()
        ) DEFAULT_VALUE else binding.mainValue.text.toString()
      CurrencyData(
        localCurrency.code, localCurrency.symbol, localCurrencyValue,
        APPC_C_SYMBOL, APPC_C_SYMBOL, appcValue
      )
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
      TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX,
        fragmentContainer.measuredWidth.toFloat(),
        requireContext().resources
          .displayMetrics
      )
        .toInt()
    val viewWidth = 80.convertDpToPx(resources)

    return screenWidth / viewWidth
  }

  override fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun changeVisibilityRefundDisclaimer(visible: Boolean) {
    binding.tvRefundDisclaimer.visibility = if (visible) View.VISIBLE else View.GONE
  }

  override fun showFee(visible: Boolean) {
    val visibility = if (visible) View.VISIBLE else View.GONE
    binding.infoFees.visibility = visibility
    binding.icInfoFees.visibility = visibility
  }
}
