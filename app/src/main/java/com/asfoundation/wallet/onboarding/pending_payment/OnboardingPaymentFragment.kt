package com.asfoundation.wallet.onboarding.pending_payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.databinding.FragmentOnboardingPaymentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingPaymentState, OnboardingPaymentSideEffect> {

  private val viewModel: OnboardingPaymentViewModel by viewModels()
  private val views by viewBinding(FragmentOnboardingPaymentBinding::bind)

  @Inject
  lateinit var navigator: OnboardingPaymentNavigator

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return FragmentOnboardingPaymentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: OnboardingPaymentState) = Unit

  override fun onSideEffect(sideEffect: OnboardingPaymentSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentSideEffect.ShowHeaderContent -> showHeaderContent(
        sideEffect.packageName,
        sideEffect.sku,
        sideEffect.currency,
        sideEffect.value
      )
    }
  }

  private fun showHeaderContent(packageName: String, sku: String, currency: String, value: Double) {
    handleAppInfo(packageName)
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameItem.text = sku
    views.onboardingPaymentHeaderLayout.onboardingPaymentBonusFiatAmount.text = value.toString()
  }

  private fun handleAppInfo(packageName: String)  {
    val pm = requireContext().packageManager
    val appInfo = pm.getApplicationInfo(packageName, 0)
    val appName = pm.getApplicationLabel(appInfo)
    val appIcon = pm.getApplicationIcon(packageName)
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameName.text = appName
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameIcon.setImageDrawable(appIcon)
  }
}