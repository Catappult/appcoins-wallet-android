package com.asfoundation.wallet.onboarding

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentOnboardingBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
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
    override fun handleOnBackPressed() = viewModel.handleBackButtonClick()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleBackPress()
    handleWalletCreationFragmentResult()
    handleTermsConditionsFragmentResult()
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

  private fun handleTermsConditionsFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
      TermsConditionsBottomSheetFragment.TERMS_CONDITIONS_COMPLETE,
      this
    ) { _, _ ->
      navigator.navigateToNavBar()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_onboarding, container, false)
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setClickListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setClickListeners() {
    views.onboardingWelcomeButtons.onboardingNextButton.setOnClickListener { viewModel.handleNextClick() }
    views.onboardingWelcomeButtons.onboardingExistentWalletButton.setOnClickListener { viewModel.handleRecoverClick() }

    views.onboardingValuesButtons.onboardingBackButton.setOnClickListener { viewModel.handleBackButtonClick() }
    views.onboardingValuesButtons.onboardingGetStartedButton.setOnClickListener { viewModel.handleGetStartedClick() }
  }

  override fun onStateChanged(state: OnboardingState) {
    when (state.pageContent) {
      OnboardingContent.EMPTY -> hideContent()
      OnboardingContent.WELCOME -> showWelcomeScreen()
      OnboardingContent.VALUES -> showValuesScreen()
    }
  }

  override fun onSideEffect(sideEffect: OnboardingSideEffect) {
    when (sideEffect) {
      OnboardingSideEffect.NavigateToLegalsConsent -> navigator.navigateToTermsBottomSheet()
      OnboardingSideEffect.NavigateToRecoverWallet -> navigator.navigateToRecover()
      OnboardingSideEffect.NavigateToWalletCreationAnimation -> navigator.navigateToCreateWalletDialog()
      OnboardingSideEffect.NavigateToExit -> {
        onBackPressedCallback.isEnabled = false
        activity?.onBackPressed()
        activity?.finishAffinity()
      }
    }
  }

  private fun showWelcomeScreen() {
    views.onboardingValuePropositions.root.visibility = View.GONE
    views.onboardingValuesButtons.root.visibility = View.GONE
    views.onboardingWalletIcon.visibility = View.VISIBLE
    views.onboardingWelcomeMessage.root.visibility = View.VISIBLE
    views.onboardingWelcomeButtons.root.visibility = View.VISIBLE
  }

  private fun showValuesScreen() {
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      views.onboardingWalletIcon.visibility = View.VISIBLE
    } else {
      views.onboardingWalletIcon.visibility = View.GONE
    }
    views.onboardingWelcomeMessage.root.visibility = View.GONE
    views.onboardingWelcomeButtons.root.visibility = View.GONE
    views.onboardingValuePropositions.root.visibility = View.VISIBLE
    views.onboardingValuesButtons.root.visibility = View.VISIBLE
  }

  private fun hideContent() {
    views.onboardingValuePropositions.root.visibility = View.GONE
    views.onboardingValuesButtons.root.visibility = View.GONE
    views.onboardingWalletIcon.visibility = View.GONE
    views.onboardingWelcomeMessage.root.visibility = View.GONE
    views.onboardingWelcomeButtons.root.visibility = View.GONE
  }

  companion object {
    const val ONBOARDING_FROM_IAP = "from_iap"
  }
}
