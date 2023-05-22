package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.util.Period
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.MergedAppcoinsLayoutBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase
import com.google.android.material.radiobutton.MaterialRadioButton
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class MergedAppcoinsFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), MergedAppcoinsView {

  companion object {
    private const val FIAT_AMOUNT_KEY = "fiat_amount"
    private const val FIAT_CURRENCY_KEY = "currency_amount"
    private const val BONUS_KEY = "bonus"
    private const val APP_NAME_KEY = "app_name"
    private const val PRODUCT_NAME_KEY = "product_name"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val IS_BDS_KEY = "is_bds"
    private const val IS_DONATION_KEY = "is_donation"
    private const val SKU_ID = "sku_id"
    private const val TRANSACTION_TYPE = "transaction_type"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val TRANSACTION_BUILDER = "transaction_builder"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val FREQUENCY = "frequency"
    const val APPC = "appcoins"
    const val CREDITS = "credits"

    @JvmStatic
    fun newInstance(
      fiatAmount: BigDecimal,
      currency: String,
      bonus: String,
      appName: String,
      productName: String?,
      appcAmount: BigDecimal,
      isBds: Boolean,
      isDonation: Boolean,
      skuId: String?,
      transactionType: String,
      gamificationLevel: Int,
      transactionBuilder: TransactionBuilder,
      isSubscription: Boolean,
      frequency: String?
    ): Fragment {
      val fragment = MergedAppcoinsFragment()
      val bundle = Bundle().apply {
        putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
        putString(FIAT_CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putString(APP_NAME_KEY, appName)
        putString(PRODUCT_NAME_KEY, productName)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putBoolean(IS_BDS_KEY, isBds)
        putBoolean(IS_DONATION_KEY, isDonation)
        putString(SKU_ID, skuId)
        putString(TRANSACTION_TYPE, transactionType)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putParcelable(TRANSACTION_BUILDER, transactionBuilder)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putString(FREQUENCY, frequency)
      }
      fragment.arguments = bundle
      return fragment
    }
  }

  private lateinit var mergedAppcoinsPresenter: MergedAppcoinsPresenter
  private var paymentSelectionSubject: PublishSubject<String>? = null
  private lateinit var iabView: IabView

  @Inject
  lateinit var billingAnalytics: BillingAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mergedAppcoinsInteractor: MergedAppcoinsInteractor

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var getWalletInfoUseCase: com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase

  @Inject
  lateinit var paymentMethodsMapper: PaymentMethodsMapper

  private val fiatAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(FIAT_AMOUNT_KEY)) {
      requireArguments().getSerializable(FIAT_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }
  private val currency: String by lazy {
    if (requireArguments().containsKey(FIAT_CURRENCY_KEY)) {
      requireArguments().getString(FIAT_CURRENCY_KEY)!!
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY)!!
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val appName: String by lazy {
    if (requireArguments().containsKey(APP_NAME_KEY)) {
      requireArguments().getString(APP_NAME_KEY)!!
    } else {
      throw IllegalArgumentException("app name data not found")
    }
  }

  private val productName: String? by lazy {
    if (requireArguments().containsKey(PRODUCT_NAME_KEY)) {
      requireArguments().getString(PRODUCT_NAME_KEY)
    } else {
      throw IllegalArgumentException("product name data not found")
    }
  }

  private val appcAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(APPC_AMOUNT_KEY)) {
      requireArguments().getSerializable(APPC_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("appc data not found")
    }
  }

  private val isBds: Boolean by lazy {
    if (requireArguments().containsKey(IS_BDS_KEY)) {
      requireArguments().getBoolean(IS_BDS_KEY)
    } else {
      throw IllegalArgumentException("is bds data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (requireArguments().containsKey(IS_DONATION_KEY)) {
      requireArguments().getBoolean(IS_DONATION_KEY)
    } else {
      throw IllegalArgumentException("is donation data not found")
    }
  }

  private val skuId: String? by lazy {
    requireArguments().getString(SKU_ID)
  }

  private val transactionType: String by lazy {
    if (requireArguments().containsKey(TRANSACTION_TYPE)) {
      requireArguments().getString(TRANSACTION_TYPE)!!
    } else {
      throw IllegalArgumentException("transaction type data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_BUILDER)) {
      (requireArguments().getParcelable(TRANSACTION_BUILDER) as TransactionBuilder?)!!
    } else {
      throw IllegalArgumentException("transaction builder not found")
    }
  }

  private val isSubscription: Boolean by lazy {
    if (requireArguments().containsKey(IS_SUBSCRIPTION)) {
      requireArguments().getBoolean(IS_SUBSCRIPTION)
    } else {
      throw IllegalArgumentException("is subscriptino data not found")
    }
  }

  private val frequency: String? by lazy {
    if (requireArguments().containsKey(FREQUENCY)) {
      requireArguments().getString(FREQUENCY)
    } else {
      null
    }
  }

  private val binding by viewBinding(MergedAppcoinsLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("DEPRECATION")
    val navigator = IabNavigator(requireFragmentManager(), activity as UriNavigator?, iabView)
    paymentSelectionSubject = PublishSubject.create()
    mergedAppcoinsPresenter =
      MergedAppcoinsPresenter(
        view = this,
        disposables = CompositeDisposable(),
        resumeDisposables = CompositeDisposable(),
        viewScheduler = AndroidSchedulers.mainThread(),
        networkScheduler = Schedulers.io(),
        analytics = billingAnalytics,
        formatter = formatter,
        getWalletInfoUseCase = getWalletInfoUseCase,
        mergedAppcoinsInteractor = mergedAppcoinsInteractor,
        gamificationLevel = gamificationLevel,
        navigator = navigator,
        logger = logger,
        transactionBuilder = transactionBuilder,
        paymentMethodsMapper = paymentMethodsMapper,
        isSubscription = isSubscription
      )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Merged Appcoins fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = MergedAppcoinsLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setHeaderInformation()
    setButtonsText()
    setBonus()
    iabView.disableBack()
    mergedAppcoinsPresenter.present(savedInstanceState)
  }

  override fun showLoading() {
    binding.paymentMethods.visibility = INVISIBLE
    binding.loadingView.visibility = VISIBLE
  }

  override fun hideLoading() {
    binding.loadingView.visibility = GONE
    binding.paymentMethods.visibility = VISIBLE
  }

  private fun setBuyButtonText(): String = when {
    isSubscription -> getString(R.string.subscriptions_subscribe_button)
    isDonation -> getString(R.string.action_donate)
    else -> getString(R.string.action_buy)
  }

  private fun setBonus() {
    if (bonus.isNotEmpty()) {
      //Build string for both landscape (header) and portrait (radio button) bonus layout
      // appcoins_radio?.bonus_value?.text = getString(R.string.gamification_purchase_header_part_2, bonus)
      binding.appcoinsRadio.appcoinsBonusLayout?.bonusValue?.text = getString(R.string.gamification_purchase_header_part_2, bonus)

      //Set visibility for both landscape (header) and portrait (radio button) bonus layout
      if (binding.appcoinsRadio.appcoinsRadioButton.isChecked) {
        binding.bonusLayout?.root?.visibility = VISIBLE
        binding.bonusMsg?.visibility = VISIBLE
      }
      binding.appcoinsRadio.appcoinsBonusLayout?.root?.visibility = VISIBLE
    } else {
      binding.appcoinsRadio.appcoinsBonusLayout?.root?.visibility = GONE
    }
  }

  private fun setHeaderInformation() {
    setNameAndDescription()
    setAppIcon()
    setPriceInformation()
  }

  override fun setPaymentsInformation(
    hasCredits: Boolean,
    creditsDisableReason: Int?,
    hasAppc: Boolean,
    appcDisabledReason: Int?
  ) {
    if (hasAppc) {
      setEnabledRadio(
        view = binding.appcoinsRadio.root,
        selectedRadioButton = binding.appcoinsRadio.appcoinsRadioButton,
        unSelectedRadioButton = binding.creditsRadio.creditsRadioButton,
        title = binding.appcoinsRadio.title,
        message = binding.appcoinsRadio.message,
        icon = binding.appcoinsRadio.icon,
        bonusView = binding.appcoinsRadio.appcoinsBonusLayout?.root,
        balanceGroup = binding.appcoinsRadio.appcBalancesGroup,
        method = APPC
      )
    } else {
      setDisabledRadio(
        view = binding.appcoinsRadio.root,
        radioButton = binding.appcoinsRadio.appcoinsRadioButton,
        title = binding.appcoinsRadio.title,
        message = binding.appcoinsRadio.message,
        icon = binding.appcoinsRadio.icon,
        bonusLayout = binding.appcoinsRadio.appcoinsBonusLayout?.root,
        balanceGroup = binding.appcoinsRadio.appcBalancesGroup,
        disabledReason = appcDisabledReason,
        defaultDisabledReason = R.string.purchase_appcoins_noavailable_body
      )
    }
    if (hasCredits) {
      setEnabledRadio(
        view = binding.creditsRadio.root,
        selectedRadioButton = binding.creditsRadio.creditsRadioButton,
        unSelectedRadioButton = binding.appcoinsRadio.appcoinsRadioButton,
        title = binding.appcoinsRadio.title,
        message = binding.appcoinsRadio.message,
        icon = binding.appcoinsRadio.icon,
        bonusView = null,
        balanceGroup = binding.creditsRadio.creditsBalancesGroup,
        method = CREDITS
      )
      binding.creditsRadio.creditsRadioButton.isChecked = true
    } else {
      setDisabledRadio(
        view = binding.creditsRadio.root,
        radioButton = binding.creditsRadio.creditsRadioButton,
        title = binding.appcoinsRadio.title,
        message = binding.appcoinsRadio.message,
        icon = binding.appcoinsRadio.icon,
        bonusLayout = null,
        balanceGroup = binding.creditsRadio.creditsBalancesGroup,
        disabledReason = creditsDisableReason,
        defaultDisabledReason = R.string.purchase_appcoins_credits_noavailable_body
      )
      binding.appcoinsRadio.appcoinsRadioButton.isChecked = hasAppc
    }
    if (hasAppc || hasCredits) {
      binding.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
    } else {
      binding.bonusLayout?.root?.visibility = INVISIBLE
      binding.bonusMsg?.visibility = INVISIBLE
    }
  }

  private fun setDisabledRadio(
    view: View,
    radioButton: MaterialRadioButton,
    title: TextView,
    message: TextView,
    icon: ImageView,
    bonusLayout: View?,
    balanceGroup: Group,
    disabledReason: Int?,
    defaultDisabledReason: Int
  ) {
    view.setOnClickListener(null)
    radioButton.setOnCheckedChangeListener(null)
    val reason = disabledReason ?: defaultDisabledReason
    radioButton.isEnabled = false
    radioButton.isChecked = false
    message.text = getString(reason)
    title.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_medium_grey))
    message.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_pink))
    bonusLayout?.setBackgroundResource(R.drawable.disable_bonus_img_background)
    message.visibility = VISIBLE
    balanceGroup.visibility = INVISIBLE
    balanceGroup.requestLayout()
    title.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    applyAlphaScale(icon)
  }

  private fun setEnabledRadio(
    view: View,
    selectedRadioButton: MaterialRadioButton,
    unSelectedRadioButton: MaterialRadioButton,
    title: TextView,
    message: TextView,
    icon: ImageView,
    bonusView: View?,
    balanceGroup: Group,
    method: String
  ) {
    view.setOnClickListener { selectedRadioButton.isChecked = true }
    selectedRadioButton.setOnCheckedChangeListener { _, checked ->
      if (checked) paymentSelectionSubject?.onNext(method)
      setTitle(checked, title)
      unSelectedRadioButton.isChecked = !checked
    }
    selectedRadioButton.isEnabled = true
    message.text = ""
    title.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_dark_grey))
    bonusView?.setBackgroundResource(R.drawable.bonus_img_background)
    message.visibility = INVISIBLE
    balanceGroup.visibility = VISIBLE
    balanceGroup.requestLayout()
    icon.colorFilter = null
  }

  private fun setTitle(checked: Boolean, title: TextView) {
    if (checked) {
      title.setTextColor(
        ContextCompat.getColor(requireContext(), R.color.styleguide_black_transparent_80)
      )
      title.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    } else {
      title.setTextColor(ContextCompat.getColor(requireContext(), R.color.styleguide_black_transparent_80))
      title.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
  }

  private fun applyAlphaScale(imageView: ImageView) {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val filter = ColorMatrixColorFilter(colorMatrix)
    imageView.colorFilter = filter
  }

  private fun getApplicationName(appPackage: String): CharSequence {
    val packageManager = requireContext().packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun getSelectedPaymentMethod(): String = when {
    binding.appcoinsRadio.appcoinsRadioButton.isChecked -> APPC
    binding.creditsRadio.creditsRadioButton.isChecked -> CREDITS
    else -> ""
  }

  override fun buyClick(): Observable<PaymentInfoWrapper> = RxView.clicks(binding.dialogBuyButtonsPaymentMethods.buyButton)
    .map {
      PaymentInfoWrapper(
        packageName = appName,
        skuDetails = skuId,
        value = appcAmount.toString(),
        purchaseDetails = getSelectedPaymentMethod(),
        transactionType = transactionType
      )
    }

  override fun backClick(): Observable<PaymentInfoWrapper> = RxView.clicks(binding.dialogBuyButtonsPaymentMethods.cancelButton)
    .map {
      PaymentInfoWrapper(
        packageName = appName,
        skuDetails = skuId,
        value = appcAmount.toString(),
        purchaseDetails = getSelectedPaymentMethod(),
        transactionType = transactionType
      )
    }

  override fun backPressed(): Observable<PaymentInfoWrapper> = iabView.backButtonPress()
    .map {
      PaymentInfoWrapper(
        packageName = appName,
        skuDetails = skuId,
        value = appcAmount.toString(),
        purchaseDetails = getSelectedPaymentMethod(),
        transactionType = transactionType
      )
    }

  override fun getPaymentSelection() = paymentSelectionSubject!!

  override fun hideBonus() {
    binding.bonusLayout?.root?.visibility = INVISIBLE
    binding.bonusMsg?.visibility = INVISIBLE
  }

  override fun showVolatilityInfo() {
    binding.info?.visibility = VISIBLE
    binding.infoText?.visibility = VISIBLE
  }

  override fun hideVolatilityInfo() {
    binding.info?.visibility = GONE
    binding.infoText?.visibility = GONE
  }

  override fun showBonus(@StringRes bonusText: Int) {
    if (bonus.isNotEmpty()) {
      val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation)
      animation.duration = 250
      binding.bonusLayout?.root?.visibility = VISIBLE
      binding.bonusLayout?.root?.startAnimation(animation)
      binding.bonusMsg?.text = getText(bonusText)
      binding.bonusMsg?.visibility = VISIBLE
    } else {
      binding.bonusLayout?.root?.visibility = GONE
      binding.bonusMsg?.visibility = GONE
    }
  }

  override fun showError(@StringRes errorMessage: Int) {
    binding.paymentMethodMainView.visibility = GONE
    binding.mergedErrorLayout.errorDismiss.setText(getString(R.string.ok))
    binding.mergedErrorLayout.genericErrorLayout.errorMessage.text = getString(errorMessage)
    binding.mergedErrorLayout.root.visibility = VISIBLE
  }

  override fun errorDismisses() = RxView.clicks(binding.mergedErrorLayout.errorDismiss)

  override fun getSupportLogoClicks() = RxView.clicks(binding.mergedErrorLayout.genericErrorLayout.layoutSupportLogo)

  override fun getSupportIconClicks() = RxView.clicks(binding.mergedErrorLayout.genericErrorLayout.layoutSupportIcn)

  override fun showAuthenticationActivity() = iabView.showAuthenticationActivity()

  override fun navigateToAppcPayment(transactionBuilder: TransactionBuilder) =
    iabView.showOnChain(fiatAmount, isBds, bonus, gamificationLevel, transactionBuilder)

  override fun navigateToCreditsPayment(transactionBuilder: TransactionBuilder) =
    iabView.showAppcoinsCreditsPayment(appcAmount, true, gamificationLevel, transactionBuilder)

  override fun updateBalanceValues(appcFiat: String, creditsFiat: String, currency: String) {
    binding.appcoinsRadio.balanceFiatAppcEth.text =
      getString(R.string.purchase_current_balance_appc_eth_body, "$appcFiat $currency")
    binding.creditsRadio.creditsFiatBalance.text =
      getString(R.string.purchase_current_balance_appcc_body, "$creditsFiat $currency")
  }

  override fun toggleSkeletons(show: Boolean) {
    if (show) {
      binding.skeletonAppcoins?.root?.visibility = VISIBLE
      binding.skeletonCredits.root.visibility = VISIBLE
      binding.paymentMethodsGroup.visibility = INVISIBLE
    } else {
      binding.skeletonAppcoins?.root?.visibility = GONE
      binding.skeletonCredits.root.visibility = GONE
      binding.paymentMethodsGroup.visibility = VISIBLE
    }
  }

  override fun onAuthenticationResult(): Observable<Boolean> = iabView.onAuthenticationResult()

  override fun showPaymentMethodsView() = iabView.showPaymentMethodsView()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mergedAppcoinsPresenter.onSavedInstanceState(outState)
  }

  override fun onResume() {
    super.onResume()
    binding.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
    mergedAppcoinsPresenter.onResume()
  }

  override fun onPause() {
    mergedAppcoinsPresenter.handlePause()
    super.onPause()
  }

  override fun onDestroyView() {
    mergedAppcoinsPresenter.handleStop()
    iabView.enableBack()
    binding.appcoinsRadio.appcoinsRadioButton.setOnCheckedChangeListener(null)
    binding.appcoinsRadio.root.setOnClickListener(null)
    binding.creditsRadio.creditsRadioButton.setOnCheckedChangeListener(null)
    binding.creditsRadio.root.setOnClickListener(null)
    super.onDestroyView()
  }

  override fun onDestroy() {
    paymentSelectionSubject = null
    super.onDestroy()
  }

  private fun setNameAndDescription() {
    if (isDonation) {
      binding.paymentMethodsHeader.appName.text = getString(R.string.item_donation)
      binding.paymentMethodsHeader.appSkuDescription.text = getString(R.string.item_donation)
    } else {
      binding.paymentMethodsHeader.appName.text = getApplicationName(appName)
      binding.paymentMethodsHeader.appSkuDescription.text = productName
    }
  }

  private fun setAppIcon() {
    try {
      binding.paymentMethodsHeader.appIcon.setImageDrawable(requireContext().packageManager.getApplicationIcon(appName))
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
  }


  private fun setPriceInformation() {
    var appcText = formatter.formatPaymentCurrency(appcAmount, WalletCurrency.APPCOINS)
      .plus(" " + WalletCurrency.APPCOINS.symbol)
    var fiatText =
      formatter.formatPaymentCurrency(fiatAmount, WalletCurrency.FIAT).plus(" $currency")
    if (isSubscription) {
      val period = Period.parse(frequency!!)
      period?.mapToSubsFrequency(requireContext(), fiatText)
        ?.let { fiatText = it }
      appcText = "~$appcText"
    }
    binding.paymentMethodsHeader.fiatPrice.text = fiatText
    binding.paymentMethodsHeader.appcPrice.text = appcText
    binding.paymentMethodsHeader.fiatPriceSkeleton.root.visibility = GONE
    binding.paymentMethodsHeader.appcPriceSkeleton.root.visibility = GONE
    binding.paymentMethodsHeader.fiatPrice.visibility = VISIBLE
    binding.paymentMethodsHeader.appcPrice.visibility = VISIBLE
  }

  private fun setButtonsText() {
    binding.dialogBuyButtonsPaymentMethods.buyButton.setText(setBuyButtonText())
    binding.dialogBuyButtonsPaymentMethods.cancelButton.setText(getString(R.string.back_button))
  }
}
