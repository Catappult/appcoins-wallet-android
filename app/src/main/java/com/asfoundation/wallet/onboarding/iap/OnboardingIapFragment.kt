package com.asfoundation.wallet.onboarding.iap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingIapFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingIapFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingIapState, OnboardingIapSideEffect> {

  private val views by viewBinding(OnboardingIapFragmentBinding::bind)

  private val viewModel: OnboardingIapViewModel by viewModels()

  @Inject
  lateinit var navigator: OnboardingIapNavigator

  private val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      activity?.finishAffinity()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addBackPressedDispatcher()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.onboarding_iap_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.onboardingIapContent.visibility = View.GONE

    viewModel.handleCreateWallet()
    handleCreateWalletFragmentResult()
    handleTermsConditionsFragmentResult()

    views.onboardingIapBackToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.onboardingExploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun addBackPressedDispatcher() {
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  private fun handleCreateWalletFragmentResult() {
    setFragmentResultListener(CreateWalletDialogFragment.CREATE_WALLET_DIALOG_COMPLETE) { _, _ ->
      showContent()
    }
  }

  private fun handleTermsConditionsFragmentResult() {
    setFragmentResultListener(TermsConditionsBottomSheetFragment.TERMS_CONDITIONS_COMPLETE) { _, _ ->
      navigator.closeOnboarding()
    }
  }

  override fun onStateChanged(state: OnboardingIapState) = Unit

  override fun onSideEffect(sideEffect: OnboardingIapSideEffect) {
    when (sideEffect) {
      OnboardingIapSideEffect.NavigateToWalletCreationAnimation -> navigator.navigateToCreateWalletDialog()
      OnboardingIapSideEffect.NavigateBackToGame -> navigator.navigateBackToGame()
      OnboardingIapSideEffect.NavigateToTermsConditions -> navigator.navigateToTermsConditionsBottomSheet()
      OnboardingIapSideEffect.ShowContent -> showContent()
    }
  }

  private fun showContent() {
    views.root.background =
      ContextCompat.getDrawable(requireContext(), R.color.blue_dark_transparent_90)
    views.onboardingIapContent.visibility = View.VISIBLE
  }

  companion object {
    @JvmStatic
    fun newInstance(): OnboardingIapFragment = OnboardingIapFragment()
  }
}

