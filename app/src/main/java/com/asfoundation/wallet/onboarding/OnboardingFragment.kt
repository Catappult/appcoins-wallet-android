package com.asfoundation.wallet.onboarding

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
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentOnboardingBinding
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingState, OnboardingSideEffect> {

  @Inject
  lateinit var navigator: OnboardingNavigator

  private val viewModel: OnboardingViewModel by viewModels()
  private val views by viewBinding(FragmentOnboardingBinding::bind)
  private val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      isEnabled = false
      activity?.onBackPressed()
      activity?.finishAffinity()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleBackPress()
    handleWalletCreationFragmentResult()
  }

  private fun handleBackPress() {
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  private fun handleWalletCreationFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
      CreateWalletDialogFragment.CREATE_WALLET_DIALOG_COMPLETE,
      this
    ) { _, _ ->
      navigator.navigateToNavBar()
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View = FragmentOnboardingBinding.inflate(inflater).root

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setClickListeners()
    setStringWithLinks()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setClickListeners() {
    views.onboardingButtons.onboardingNextButton.setOnClickListener { viewModel.handleLaunchWalletClick() }
    views.onboardingButtons.onboardingExistentWalletButton.setOnClickListener { viewModel.handleRecoverClick() }
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

  override fun onSideEffect(sideEffect: OnboardingSideEffect) {
    when (sideEffect) {
      OnboardingSideEffect.NavigateToRecoverWallet -> navigator.navigateToRecover()
      OnboardingSideEffect.NavigateToWalletCreationAnimation -> {
        hideContent()
        navigator.navigateToCreateWalletDialog()
      }
      OnboardingSideEffect.NavigateToFinish -> navigator.navigateToNavBar()
      is OnboardingSideEffect.NavigateToLink -> navigator.navigateToBrowser(sideEffect.uri)
    }
  }

  private fun showValuesScreen() {
    views.onboardingWalletIcon?.visibility = View.VISIBLE
    views.onboardingValuePropositions.root.visibility = View.VISIBLE
    views.onboardingButtons.root.visibility = View.VISIBLE
    views.onboardingTermsConditions.root.visibility = View.VISIBLE
  }

  private fun hideContent() {
    views.onboardingWalletIcon?.visibility = View.GONE
    views.onboardingValuePropositions.root.visibility = View.GONE
    views.onboardingButtons.root.visibility = View.GONE
    views.onboardingTermsConditions.root.visibility = View.GONE
  }

  private fun setStringWithLinks() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox =
      resources.getString(
        R.string.intro_agree_terms_and_conditions_body, termsConditions,
        privacyPolicy
      )

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, Uri.parse(TERMS_CONDITIONS_URL))
    setLinkToString(spannableString, privacyPolicy, Uri.parse(PRIVACY_POLICY_URL))

    views.onboardingTermsConditions.termsConditionsBody.text = spannableString
    views.onboardingTermsConditions.termsConditionsBody.isClickable = true
    views.onboardingTermsConditions.termsConditionsBody.movementMethod =
      LinkMovementMethod.getInstance()
  }

  private fun setLinkToString(
    spannableString: SpannableString, highlightString: String,
    uri: Uri
  ) {
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(widget: View) {
        viewModel.handleLinkClick(uri = uri)
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
}