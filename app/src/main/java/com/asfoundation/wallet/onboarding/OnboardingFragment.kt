package com.asfoundation.wallet.onboarding

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
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import javax.inject.Inject

class OnboardingFragment : BasePageViewFragment(),
    SingleStateFragment<OnboardinState, OnboardingSideEffect> {

  @Inject
  lateinit var onboardingViewModelFactory: OnboardingViewModelFactory

  @Inject
  lateinit var navigator: OnboardingNavigator

  private val viewModel: OnboardingViewModel by viewModels { onboardingViewModelFactory }
  private val views by viewBinding(FragmentOnboardingBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleBackPress()
    handleFragmentResult()
  }

  private fun handleBackPress() {
    requireActivity().onBackPressedDispatcher.addCallback(this,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            when (viewModel.state.pageNumber) {
              0 -> {
                // Do nothing
              }
              1 -> showWelcomeScreen()
            }
          }
        })
  }

  private fun handleFragmentResult() {
    childFragmentManager.setFragmentResultListener("CreateWalletDialogFragment",
        this) { requestKey, bundle ->
      val resultReceived = bundle.getString("fragmentEnded")
      // do something with the result
      navigator.navigateToMainActivity(fromSupportNotification = false)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                            @Nullable savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_onboarding, container, false)
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    showWelcomeScreen()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: OnboardinState) = Unit

  override fun onSideEffect(sideEffect: OnboardingSideEffect) {
    when (sideEffect) {
      OnboardingSideEffect.NavigateToRecoverWallet -> navigator.navigateToRestoreActivity()
      OnboardingSideEffect.NavigateToValuePropositions -> showValuesScreen()
      OnboardingSideEffect.NavigateBackToWelcomeScreen -> showWelcomeScreen()
    }
  }

  private fun showWelcomeScreen() {
    views.onboardingWelcomeMessage.onboardingWelcomeMessageLayout.visibility = View.VISIBLE
    views.onboardingWelcomeButtons.onboardingWelcomeButtonsLayout.visibility = View.VISIBLE

    views.onboardingWelcomeButtons.onboardingNextButton.setOnClickListener { viewModel.handleNextClick() }
    views.onboardingWelcomeButtons.onboardingExistentWalletButton.setOnClickListener { viewModel.handleRecoverClick() }

    views.onboardingValuePropositions.onboardingValuePropositionsLayout.visibility = View.GONE
    views.onboardingValuePropositionButtons.onboardingValuePropositionsLayout.visibility = View.GONE
  }

  private fun showValuesScreen() {
    views.onboardingValuePropositions.onboardingValuePropositionsLayout.visibility = View.VISIBLE
    views.onboardingValuePropositionButtons.onboardingValuePropositionsLayout.visibility =
        View.VISIBLE

    views.onboardingValuePropositionButtons.onboardingBackButton.setOnClickListener { viewModel.handleBackButtonClick() }
    views.onboardingValuePropositionButtons.onboardingGetStartedButton.setOnClickListener {
      navigator.navigateToTermsBottomSheet()
    }

    views.onboardingWelcomeMessage.onboardingWelcomeMessageLayout.visibility = View.GONE
    views.onboardingWelcomeButtons.onboardingWelcomeButtonsLayout.visibility = View.GONE
  }

  companion object {
    fun newInstance() = OnboardingFragment()
  }
}