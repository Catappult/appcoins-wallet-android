package com.asfoundation.wallet.onboarding.iap

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingIapFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.onboarding.OnboardingFragment
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

    handleTermsConditionsFragmentResult()
    viewModel.handleLoadIcon()

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

  private fun handleTermsConditionsFragmentResult() {
    setFragmentResultListener(OnboardingFragment.ONBOARDING_FINISHED_KEY) { _, _ ->
      navigator.closeOnboarding()
    }
  }

  override fun onStateChanged(state: OnboardingIapState) = Unit

  override fun onSideEffect(sideEffect: OnboardingIapSideEffect) {
    when (sideEffect) {
      OnboardingIapSideEffect.NavigateBackToGame -> navigator.navigateBackToGame()
      OnboardingIapSideEffect.NavigateToTermsConditions -> navigator.navigateToTermsConditionsBottomSheet()
      is OnboardingIapSideEffect.LoadPackageNameIcon -> sideEffect.appPackageName?.let {
        loadPackageNameIcon(it)
      }
    }
  }

  private fun loadPackageNameIcon(appPackageName: String) {
    try {
      val appIcon = requireContext().packageManager.getApplicationIcon(appPackageName)
      views.onboardingIapGameIcon.setImageDrawable(appIcon)
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
  }

  companion object {
    @JvmStatic
    fun newInstance(): OnboardingIapFragment = OnboardingIapFragment()
  }
}

