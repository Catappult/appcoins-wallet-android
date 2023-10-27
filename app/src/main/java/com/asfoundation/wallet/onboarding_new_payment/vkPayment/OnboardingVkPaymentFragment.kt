package com.asfoundation.wallet.onboarding_new_payment.vkPayment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.vkpay.VkPayManager
import com.appcoins.wallet.sharedpreferences.VkDataPreferencesDataSource
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingVkPaymentLayoutBinding
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.superapp.SuperappKit
import com.vk.superapp.vkpay.checkout.VkCheckoutResult
import com.vk.superapp.vkpay.checkout.VkCheckoutResultDisposable
import com.vk.superapp.vkpay.checkout.VkPayCheckout
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingVkPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingVkPaymentState, OnboardingVkPaymentSideEffect> {

  private val viewModel: OnboardingVkPaymentViewModel by viewModels()
  private val binding by lazy { OnboardingVkPaymentLayoutBinding.bind(requireView()) }
  lateinit var args: OnboardingVkPaymentFragmentArgs

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var vkDataPreferencesDataSource: VkDataPreferencesDataSource

  private val authVkCallback = object : VkClientAuthCallback {
    override fun onAuth(authResult: AuthResult) {
      vkDataPreferencesDataSource.saveAuthVk(authResult.accessToken)
      startVkCheckoutPay()
    }
  }

  private var observeCheckoutResults: VkCheckoutResultDisposable =
    VkPayCheckout.observeCheckoutResult { result
      ->
      handleCheckoutResult(result)
    }


  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return OnboardingVkPaymentLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingVkPaymentFragmentArgs.fromBundle(requireArguments())
    //Build Vk Pay SuperApp Kit
    VkPayManager.initSuperAppKit(BuildConfig.VK_APP_NAME, BuildConfig.VK_CLIENT_SECRET, requireContext(), R.mipmap.ic_launcher, BuildConfig.VK_SDK_APP_ID, activity)
    VkClientAuthLib.addAuthCallback(authVkCallback)
    viewModel.getPaymentLink()
  }

  private fun handleCheckoutResult(vkCheckoutResult: VkCheckoutResult) {
    if (vkCheckoutResult.orderId.isNotEmpty()) {
      viewModel.startTransactionStatusTimer()
    }
  }

  private fun startVkCheckoutPay() {
    val hash = viewModel.state.vkTransaction.value?.hash
    val uidTransaction = viewModel.state.vkTransaction.value?.uid
    val amount = viewModel.state.vkTransaction.value?.amount
    if (hash != null && uidTransaction != null && amount != null) {
      VkPayManager.checkoutVkPay(hash, uidTransaction, viewModel.walletAddress,  amount, BuildConfig.VK_MERCHANT_ID.toInt(), BuildConfig.VK_SDK_APP_ID.toInt(), requireFragmentManager())
    } else {
      showError()
    }
    observeCheckoutResults = VkPayCheckout.observeCheckoutResult { handleCheckoutResult(it) }
  }


  override fun onStateChanged(state: OnboardingVkPaymentState) {
    when (state.vkTransaction) {
      Async.Uninitialized,
      is Async.Loading -> {

      }

      is Async.Success -> {
        if (SuperappKit.isInitialized()) {
          viewModel.transactionUid = state.vkTransaction.value?.uid
          if (vkDataPreferencesDataSource.getAuthVk().isNullOrEmpty()) {
            binding.vkFastLoginButton.performClick()
          } else {
            startVkCheckoutPay()
          }
        }
      }

      is Async.Fail -> {
        showError()
      }

      else -> {}
    }
  }

  fun showError() {
    binding.loading.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.unknown_error)
    binding.errorView.root.visibility = View.VISIBLE
  }

  private fun showCompletedPayment() {
    binding.loading.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.playAnimation()
  }

  override fun onSideEffect(sideEffect: OnboardingVkPaymentSideEffect) {
    when (sideEffect) {
      is OnboardingVkPaymentSideEffect.ShowError -> { showError()}
      OnboardingVkPaymentSideEffect.ShowLoading -> {}
      OnboardingVkPaymentSideEffect.ShowSuccess -> { showCompletedPayment()}
    }
  }

  companion object {
    const val PAYMENT_DATA = "data"
  }
}
