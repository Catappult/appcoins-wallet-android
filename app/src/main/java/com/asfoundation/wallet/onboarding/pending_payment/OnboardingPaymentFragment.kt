package com.asfoundation.wallet.onboarding.pending_payment

import android.annotation.SuppressLint
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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.AppUtils
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.appcoins.wallet.core.utils.properties.UrlPropertiesFormatter
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentOnboardingPaymentBinding
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.asfoundation.wallet.onboarding_new_payment.payment_result.OnboardingSharedHeaderViewModel
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingPaymentState, OnboardingPaymentSideEffect> {

  private val viewModel: OnboardingPaymentViewModel by viewModels()
  private val views by viewBinding(FragmentOnboardingPaymentBinding::bind)

  private lateinit var innerNavHostFragment: NavHostFragment

  @Inject
  lateinit var navigator: OnboardingPaymentNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return FragmentOnboardingPaymentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    editToolbar()
    initInnerNavController()
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    handlePaymentFinishResult()
    setStringWithLinks()
    val sharedHeaderViewModel =
      ViewModelProvider(requireActivity())[OnboardingSharedHeaderViewModel::class.java]
    // Observe the LiveData for visibility changes
    sharedHeaderViewModel.viewVisibility.observe(viewLifecycleOwner) { visibility ->
      views.onboardingPaymentHeaderLayout.root.visibility = visibility
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun handlePaymentFinishResult() {
    viewModel.setOnboardingCompleted()
    innerNavHostFragment.childFragmentManager.setFragmentResultListener(
      ONBOARDING_PAYMENT_CONCLUSION,
      this
    ) { _, _ ->
      views.root.visibility = View.GONE
      context?.let {
        lifecycleScope.launch {
          AppUtils.restartApp(requireActivity())
        }
      }
    }
  }

  private fun editToolbar() {
    views.toolbar.actionButtonSettings.visibility = View.GONE
    views.toolbar.actionButtonSupport.visibility = View.GONE
  }

  private fun initInnerNavController() {
    innerNavHostFragment = childFragmentManager.findFragmentById(
      R.id.onboarding_payment_fragment_container
    ) as NavHostFragment
  }

  override fun onStateChanged(state: OnboardingPaymentState) {
    when (state.transactionContent) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO add a skeleton while the list loads
        views.loadingAnimation.playAnimation()
      }

      is Async.Success -> {
        state.transactionContent()?.let { showHeaderContent(it) }
      }

      is Async.Fail -> showRetryError()
    }
  }

  private fun showRetryError() {
    views.loadingAnimation.visibility = View.GONE
    views.onboardingPaymentHeaderLayout.root.visibility = View.GONE
    views.onboardingPaymentErrorLayout?.root?.visibility = View.VISIBLE
    views.onboardingPaymentErrorLayout?.tryAgain?.setOnClickListener {
      showLoading()
      viewModel.handleContent()
    }
  }

  private fun showLoading() {
    views.loadingAnimation.visibility = View.VISIBLE
    views.onboardingPaymentHeaderLayout.root.visibility = View.GONE
    views.onboardingPaymentErrorLayout?.root?.visibility = View.GONE
  }

  override fun onSideEffect(sideEffect: OnboardingPaymentSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentSideEffect.ShowPaymentMethods -> navigator.showPaymentMethods(
        innerNavHostFragment.navController,
        sideEffect.transactionContent.transactionBuilder,
        sideEffect.transactionContent.packageName,
        sideEffect.transactionContent.sku,
        sideEffect.transactionContent.value,
        sideEffect.transactionContent.currency,
        sideEffect.transactionContent.forecastBonus
      )

      is OnboardingPaymentSideEffect.showOrHideRefundDisclaimer -> {
        views.onboardingPaymentTermsConditions?.disclaimerBody?.visibility =
          if (sideEffect.showOrHideRefundDisclaimer) View.VISIBLE else View.GONE
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun showHeaderContent(transactionContent: TransactionContent) {
    views.loadingAnimation.visibility = View.GONE
    views.onboardingPaymentErrorLayout?.root?.visibility = View.GONE
    views.onboardingPaymentHeaderLayout.root.visibility = View.VISIBLE
    handleAppInfo(transactionContent.packageName)
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameItem.text = transactionContent.skuTitle
    views.onboardingPaymentHeaderLayout.onboardingPaymentBonusText.text =
      getString(
        R.string.gamification_purchase_header_part_2,
        transactionContent.forecastBonus.getPurchaseBonusMessage(formatter)
      )
    views.onboardingPaymentHeaderLayout.onboardingPaymentBonusFiatAmount.text =
      "${transactionContent.currencySymbol}${transactionContent.value}"
  }

  private fun handleAppInfo(packageName: String) {
    val pm = requireContext().packageManager
    val appInfo = pm.getApplicationInfo(packageName, 0)
    val appName = pm.getApplicationLabel(appInfo)
    val appIcon = pm.getApplicationIcon(packageName)
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameName.text = appName
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameIcon.setImageDrawable(appIcon)
  }

  private fun setStringWithLinks() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox =
      resources.getString(
        R.string.agree_by_choosing_a_payment_method_body, termsConditions,
        privacyPolicy
      )

    val termsConditionsUrl = UrlPropertiesFormatter.addLanguageElementToUrl(TERMS_CONDITIONS_URL)
    val privacyPolicyUrl = UrlPropertiesFormatter.addLanguageElementToUrl(PRIVACY_POLICY_URL)

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, termsConditionsUrl)
    setLinkToString(spannableString, privacyPolicy, privacyPolicyUrl)

    views.onboardingPaymentTermsConditions?.termsConditionsBody?.text = spannableString
    views.onboardingPaymentTermsConditions?.termsConditionsBody?.isClickable = true
    views.onboardingPaymentTermsConditions?.termsConditionsBody?.movementMethod =
      LinkMovementMethod.getInstance()
  }

  private fun setLinkToString(
    spannableString: SpannableString, highlightString: String,
    uri: Uri
  ) {
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(widget: View) {
        navigator.navigateToBrowser(uri)
      }

      override fun updateDrawState(ds: TextPaint) {
        ds.color = ResourcesCompat.getColor(resources, R.color.styleguide_pink, null)
        ds.isUnderlineText = true
      }
    }
    val indexHighlightString = spannableString.toString()
      .indexOf(highlightString)
    val highlightStringLength = highlightString.length
    spannableString.setSpan(
      clickableSpan, indexHighlightString,
      indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
      StyleSpan(Typeface.BOLD), indexHighlightString,
      indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  companion object {
    const val ONBOARDING_PAYMENT_CONCLUSION = "onboarding_payment_conclusion"
  }
}