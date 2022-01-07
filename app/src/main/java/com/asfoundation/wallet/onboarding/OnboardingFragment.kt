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
                isEnabled = false
                activity?.onBackPressed()
              }
              1 -> viewModel.handleBackButtonClick()
            }
          }
        })
  }

  private fun handleFragmentResult() {
    parentFragmentManager.setFragmentResultListener(
        CreateWalletDialogFragment.RESULT_REQUEST_KEY,
        this
    ) { _, _ ->
      navigator.navigateToMainActivity(fromSupportNotification = false)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                            @Nullable savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_onboarding, container, false)
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setClickListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setClickListeners() {
    views.onboardingWelcomeMessage.onboardingNextButton.setOnClickListener { viewModel.handleNextClick() }
    views.onboardingWelcomeMessage.onboardingExistentWalletButton.setOnClickListener { viewModel.handleRecoverClick() }

    views.onboardingValuePropositions.onboardingBackButton.setOnClickListener { viewModel.handleBackButtonClick() }
    views.onboardingValuePropositions.onboardingGetStartedButton.setOnClickListener { navigator.navigateToTermsBottomSheet() }
  }

  override fun onStateChanged(state: OnboardingState) {
    when (state.pageNumber) {
      0 -> showWelcomeScreen()
      1 -> showValuesScreen()
    }
  }

  override fun onSideEffect(sideEffect: OnboardingSideEffect) {
    when (sideEffect) {
      OnboardingSideEffect.NavigateToRecoverWallet -> navigator.navigateToRestoreActivity()
    }
  }

  private fun showWelcomeScreen() {
    views.onboardingValuePropositions.root.visibility = View.GONE
    views.onboardingWalletIcon.visibility = View.VISIBLE
    views.onboardingWelcomeMessage.root.visibility = View.VISIBLE
  }

  private fun showValuesScreen() {
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      views.onboardingWalletIcon.visibility = View.VISIBLE
    } else {
      views.onboardingWalletIcon.visibility = View.GONE
    }
    views.onboardingWelcomeMessage.root.visibility = View.GONE
    views.onboardingValuePropositions.root.visibility = View.VISIBLE
  }

  companion object {
    fun newInstance() = OnboardingFragment()
  }
}