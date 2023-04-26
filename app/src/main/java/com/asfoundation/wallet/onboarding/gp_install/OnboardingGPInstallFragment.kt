package com.asfoundation.wallet.onboarding.gp_install

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.databinding.OnboardingGpInstallFragmentBinding
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingGPInstallFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingGPInstallState, OnboardingGPInstallSideEffect> {

  private val views by viewBinding(OnboardingGpInstallFragmentBinding::bind)

  private val viewModel: OnboardingGPInstallViewModel by viewModels()

  @Inject
  lateinit var navigator: OnboardingGPInstallNavigator

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
  ): View = OnboardingGpInstallFragmentBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
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

  override fun onStateChanged(state: OnboardingGPInstallState) = Unit

  override fun onSideEffect(sideEffect: OnboardingGPInstallSideEffect) {
    when (sideEffect) {
      is OnboardingGPInstallSideEffect.NavigateBackToGame -> navigator.navigateBackToGame(sideEffect.appPackageName)
      OnboardingGPInstallSideEffect.NavigateToExploreWallet -> views.root.visibility = View.GONE
      is OnboardingGPInstallSideEffect.LoadPackageNameIcon -> loadPackageNameIcon(sideEffect.appPackageName)
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
}

