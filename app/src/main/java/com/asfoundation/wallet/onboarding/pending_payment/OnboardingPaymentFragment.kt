package com.asfoundation.wallet.onboarding.pending_payment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentOnboardingPaymentBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingPaymentFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null),
  SingleStateFragment<OnboardingPaymentState, OnboardingPaymentSideEffect> {

  private val viewModel: OnboardingPaymentViewModel by viewModels()
  private val views by viewBinding(FragmentOnboardingPaymentBinding::bind)

  private lateinit var innerNavHostFragment: NavHostFragment

  @Inject
  lateinit var navigator: OnboardingPaymentNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return FragmentOnboardingPaymentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    editToolbar()
    initInnerNavController()
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    handlePaymentFinishResult()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun handlePaymentFinishResult() {
    innerNavHostFragment.childFragmentManager.setFragmentResultListener(
      ONBOARDING_PAYMENT_CONCLUSION,
      this
    ) { _, _ ->
      views.root.visibility = View.GONE
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
    }
  }

  private fun showHeaderContent(transactionContent: TransactionContent) {
    views.loadingAnimation.visibility = View.GONE
    views.onboardingPaymentErrorLayout?.root?.visibility = View.GONE
    views.onboardingPaymentHeaderLayout.root.visibility = View.VISIBLE
    handleAppInfo(transactionContent.packageName)
    views.onboardingPaymentHeaderLayout.onboardingPaymentGameItem.text = transactionContent.skuTitle
    views.onboardingPaymentHeaderLayout.onboardingPaymentBonusText.text =
      getString(
        R.string.bonus_body,
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

  companion object {
    const val ONBOARDING_PAYMENT_CONCLUSION = "onboarding_payment_conclusion"
  }
}