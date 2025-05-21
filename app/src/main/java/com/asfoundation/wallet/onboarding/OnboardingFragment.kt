package com.asfoundation.wallet.onboarding

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.AppUtils
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.appcoins.wallet.core.utils.properties.UrlPropertiesFormatter
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentOnboardingBinding
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.ui.webview_login.WebViewLoginActivity
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingState, OnboardingSideEffect> {

  @Inject
  lateinit var navigator: OnboardingNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val args by navArgs<OnboardingFragmentArgs>()

  private val backupModel by lazy { args.backupModel ?: BackupModel() }
  private val createWalletAutomatically by lazy { args.createWalletAutomatically }

  private val viewModel: OnboardingViewModel by viewModels()
  private val views by viewBinding(FragmentOnboardingBinding::bind)
  private val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      isEnabled = false
      activity?.onBackPressed()
      activity?.finishAffinity()
    }
  }

  private val openLoginLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      viewModel.handleOpenLoginResult(result.resultCode == Activity.RESULT_OK)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleBackPress()
    lockRotation()
    if (createWalletAutomatically) {
      createWalletAutomatically()
    }
  }

  override fun onResume() {
    super.onResume()
    handleWalletCreationFragmentResult()
  }

  private fun handleBackPress() {
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  private fun handleWalletCreationFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
      CreateWalletDialogFragment.CREATE_WALLET_DIALOG_COMPLETE, this
    ) { _, _ ->
      navigator.navigateToNavBar()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentOnboardingBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.onboardingValuePropositions.onboardingValue1Body.text =
      getString(R.string.onboarding_bonus_title, "20")

    setClickListeners()
    setStringWithLinks()
    handleRecoverGuestWallet()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setClickListeners() {
    views.onboardingButtons.onboardingNextButton.setOnClickListener {
      viewModel.handleLaunchWalletClick()
    }
    views.onboardingButtons.onboardingExistentWalletButton.setOnClickListener {
      viewModel.handleRecoverClick()
    }
    views.onboardingRecoverGuestButton.setOnClickListener {
      viewModel.handleRecoverAndVerifyGuestWalletClick(backupModel)
    }
    views.onboardingGuestLaunchButton.setOnClickListener {
      viewModel.handleLaunchWalletClick()
    }
    views.onboardingGuestVerifyButton.setOnClickListener {
      viewModel.handleRecoverAndVerifyGuestWalletClick(backupModel)
    }
  }

  override fun onStateChanged(state: OnboardingState) {
    handlePageContent(state.pageContent)
  }

  private fun handlePageContent(pageContent: OnboardingContent) {
    when (pageContent) {
      OnboardingContent.EMPTY -> hideContent()
      OnboardingContent.VALUES -> showValuesScreen()
    }
  }

  private fun handleRecoverGuestWallet() {
    if (backupModel.backupPrivateKey.isNotBlank()) {
      when (backupModel.flow) {
        OnboardingFlow.VERIFY_CREDIT_CARD.name,
        OnboardingFlow.VERIFY_PAYPAL.name ->
          showVerifyGuestWallet()

        OnboardingFlow.ONBOARDING_PAYMENT.name ->
          viewModel.handleRecoverAndVerifyGuestWalletClick(backupModel)

        else -> {
          viewModel.getGuestWalletBonus(backupModel.backupPrivateKey)
          showRecoverGuestWallet()
        }
      }
    } else {
      showDefaultOnboardingLayout()
    }
  }

  override fun onSideEffect(sideEffect: OnboardingSideEffect) {
    when (sideEffect) {
      OnboardingSideEffect.NavigateToRecoverWallet -> navigator.navigateToRecover()
      is OnboardingSideEffect.NavigateToWalletCreationAnimation -> {
        hideContent()
        navigator.navigateToCreateWalletDialog(isPayment = sideEffect.isPayment)
      }

      OnboardingSideEffect.NavigateToFinish -> {
        unlockRotation()
        restart()
      }

      is OnboardingSideEffect.NavigateToLink ->
        navigator.navigateToBrowser(sideEffect.uri)

      OnboardingSideEffect.ShowLoadingRecover ->
        showRecoveringGuestWalletLoading()

      is OnboardingSideEffect.UpdateGuestBonus ->
        showGuestBonus(sideEffect.bonus)

      is OnboardingSideEffect.NavigateToVerify ->
        navigator.navigateToVerify(sideEffect.flow)

      OnboardingSideEffect.NavigateToOnboardingPayment ->
        navigator.navigateToOnboardingPayment()

      OnboardingSideEffect.OpenLogin -> {
//        navigator.navigateToLogin()

        //TODO:
        val url =
          "https://wallet.dev.aptoide.com/pt_PT/wallet/sign-in?domain=com.appcoins.wallet.dev&payment_channel=wallet_app"
        val intent = Intent(requireContext(), WebViewLoginActivity::class.java)
        intent.putExtra(WebViewLoginActivity.URL, url)
        openLoginLauncher.launch(intent)
      }
    }
  }

  private fun restart() {
    lifecycleScope.launch {
      AppUtils.restartApp(requireActivity(), copyIntent = createWalletAutomatically)
    }
  }

  private fun showRecoverGuestWallet() {
    views.loading.visibility = View.GONE
    views.onboardingContent.visibility = View.VISIBLE
    views.onboardingAction.visibility = View.GONE
    views.onboardingRecoverGuestWallet.visibility = View.VISIBLE
    views.onboardingRecoverText2.text = getString(
      R.string.monetary_amount_with_symbol, "$", "0.00"
    )
    views.onboardingRecoverText2.visibility = View.INVISIBLE
    views.onboardingRecoverText3.visibility = View.INVISIBLE
    views.onboardingBonusImage.visibility = View.INVISIBLE
    views.bonusLoading.visibility = View.VISIBLE
    views.onboardingRecoverText5.visibility = View.INVISIBLE
    views.loadingAnimation.visibility = View.INVISIBLE
  }

  private fun showDefaultOnboardingLayout() {
    views.loading.visibility = View.GONE
    views.onboardingContent.visibility = View.VISIBLE
    views.onboardingAction.visibility = View.VISIBLE
  }

  private fun showVerifyGuestWallet() {
    views.onboardingContent.visibility = View.VISIBLE
    views.onboardingRecoverGuestWallet.visibility = View.GONE
    views.onboardingVerifyGuestWallet.visibility = View.VISIBLE
    views.loadingAnimation.visibility = View.INVISIBLE
    views.onboardingVerifyLaunchText.visibility = View.INVISIBLE
    views.onboardingAction.visibility = View.GONE

  }

  private fun showRecoveringGuestWalletLoading() {
    views.onboardingRecoverText5.visibility = View.VISIBLE
    views.loadingAnimation.visibility = View.VISIBLE
    views.loadingVerifyAnimation.visibility = View.VISIBLE
    views.onboardingVerifyLaunchText.visibility = View.VISIBLE
    views.onboardingRecoverGuestButton.visibility = View.INVISIBLE
    views.onboardingGuestLaunchButton.visibility = View.INVISIBLE
    views.onboardingGuestVerifyButton.visibility = View.INVISIBLE
    views.orText.visibility = View.INVISIBLE
    views.leftLine.visibility = View.INVISIBLE
    views.rightLine.visibility = View.INVISIBLE
  }

  private fun showGuestBonus(bonus: FiatValue) {
    views.onboardingContent.visibility = View.VISIBLE
    views.onboardingRecoverText2.text = getString(
      R.string.monetary_amount_with_symbol,
      bonus.symbol,
      formatter.formatCurrency(bonus.amount, WalletCurrency.FIAT)
    )
    views.onboardingRecoverText2.visibility = View.VISIBLE
    views.onboardingRecoverText3.visibility = View.VISIBLE
    views.onboardingBonusImage.visibility = View.VISIBLE
    views.bonusLoading.visibility = View.GONE
  }

  private fun showValuesScreen() {
    views.loading.visibility = View.GONE
    views.onboardingContent.visibility = View.VISIBLE
    views.onboardingWalletIcon?.visibility = View.VISIBLE
    views.onboardingValuePropositions.root.visibility = View.VISIBLE
    views.onboardingButtons.root.visibility = View.VISIBLE
    views.onboardingTermsConditions.root.visibility = View.VISIBLE
  }

  private fun hideContent() {
    views.onboardingContent.visibility = View.GONE
    views.onboardingWalletIcon?.visibility = View.GONE
    views.onboardingValuePropositions.root.visibility = View.GONE
    views.onboardingButtons.root.visibility = View.GONE
    views.onboardingTermsConditions.root.visibility = View.GONE
  }

  private fun setStringWithLinks() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox = resources.getString(
      R.string.intro_agree_terms_and_conditions_body, termsConditions, privacyPolicy
    )

    val termsConditionsUrl = UrlPropertiesFormatter.addLanguageElementToUrl(TERMS_CONDITIONS_URL)
    val privacyPolicyUrl = UrlPropertiesFormatter.addLanguageElementToUrl(PRIVACY_POLICY_URL)

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, termsConditionsUrl)
    setLinkToString(spannableString, privacyPolicy, privacyPolicyUrl)

    views.onboardingTermsConditions.termsConditionsBody.text = spannableString
    views.onboardingTermsConditions.termsConditionsBody.setTextColor(
      resources.getColor(
        R.color.styleguide_dark_grey,
        requireActivity().theme
      )
    )
    views.onboardingTermsConditions.termsConditionsBody.isClickable = true
    views.onboardingTermsConditions.termsConditionsBody.movementMethod =
      LinkMovementMethod.getInstance()
  }

  private fun setLinkToString(
    spannableString: SpannableString,
    highlightString: String,
    uri: Uri
  ) {
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(widget: View) {
        viewModel.handleLinkClick(uri = uri)
      }

      override fun updateDrawState(ds: TextPaint) {
        ds.color = ResourcesCompat.getColor(resources, R.color.styleguide_payments_background, null)
        ds.isUnderlineText = true
      }
    }
    val indexHighlightString = spannableString.toString().indexOf(highlightString)
    val highlightStringLength = highlightString.length
    spannableString.setSpan(
      clickableSpan,
      indexHighlightString,
      indexHighlightString + highlightStringLength,
      Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
      StyleSpan(Typeface.BOLD),
      indexHighlightString,
      indexHighlightString + highlightStringLength,
      Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  fun unlockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  fun createWalletAutomatically() {
    if (!backupModel.isForRecoverWallet()) {
      viewModel.handleLaunchWalletClick()
    } else {
      viewModel.handleRecoverAndVerifyGuestWalletClick(backupModel)
    }
  }

}