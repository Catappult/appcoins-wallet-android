package com.asfoundation.wallet.onboarding_new_payment.vkPayment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.vkpay.VkPayManager
import com.appcoins.wallet.sharedpreferences.VkDataPreferencesDataSource
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingVkPaymentLayoutBinding
import com.asfoundation.wallet.billing.vkpay.VkPaymentIABFragment
import com.asfoundation.wallet.onboarding_new_payment.adyen_payment.OnboardingAdyenPaymentNavigator
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.superapp.SuperappKit
import com.vk.superapp.vkpay.checkout.VkCheckoutResult
import com.vk.superapp.vkpay.checkout.VkCheckoutSuccess
import com.vk.superapp.vkpay.checkout.VkPayCheckout
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingVkPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingVkPaymentStates, OnboardingVkPaymentSideEffect> {

  private val viewModel: OnboardingVkPaymentViewModel by viewModels()
  private val binding by lazy { OnboardingVkPaymentLayoutBinding.bind(requireView()) }
  lateinit var args: OnboardingVkPaymentFragmentArgs

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var vkDataPreferencesDataSource: VkDataPreferencesDataSource

  @Inject
  lateinit var vkPayManager: VkPayManager

  @Inject
  lateinit var navigator: OnboardingVkPaymentNavigator

  private val authVkCallback = object : VkClientAuthCallback {
    override fun onAuth(authResult: AuthResult) {
      val email = authResult.personalData?.email ?: ""
      val phone = authResult.personalData?.phone ?: ""
      vkDataPreferencesDataSource.saveAuthVk(
        accessToken = authResult.accessToken,
        email = email,
        phone = phone
      )
      startTransaction(email, phone)
    }

    override fun onCancel() {
      super.onCancel()
      showError()
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    //Build Vk Pay SuperApp Kit
    vkPayManager.initSuperAppKit(
      BuildConfig.VK_APP_NAME,
      BuildConfig.VK_CLIENT_SECRET,
      requireContext(),
      R.mipmap.ic_launcher,
      BuildConfig.VK_SDK_APP_ID,
      activity
    )
    VkClientAuthLib.addAuthCallback(authVkCallback)
    return OnboardingVkPaymentLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingVkPaymentFragmentArgs.fromBundle(requireArguments())
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    lifecycleScope.launch {
      delay(500)  // necessary delay to ensure the superappKit is actually ready.
      if (SuperappKit.isInitialized()) {
        if (vkDataPreferencesDataSource.getAuthVk().isNullOrEmpty()) {
          binding.vkFastLoginButton.performClick()
        } else {
          startTransaction(
            email = vkDataPreferencesDataSource.getEmailVK() ?: "",
            phone = vkDataPreferencesDataSource.getPhoneVK() ?: ""
          )
        }
      }
    }
  }

  private fun startTransaction(email: String, phone: String) {
    if (viewModel.isFirstGetPaymentLink) {
      viewModel.getPaymentLink(email, phone)
    }
  }

  private fun handleCheckoutResult(vkCheckoutResult: VkCheckoutResult) {
    when (vkCheckoutResult) {
      is VkCheckoutSuccess -> {}
      else -> {
        showError()
      }
    }
  }

  private fun startVkCheckoutPay() {
    val hash = viewModel.state.vkTransaction.value?.hash
    val uidTransaction = viewModel.state.vkTransaction.value?.uid
    val amount = viewModel.state.vkTransaction.value?.amount
    if (hash != null && uidTransaction != null && amount != null) {
      vkPayManager.checkoutVkPay(
        hash,
        uidTransaction,
        vkDataPreferencesDataSource.getEmailVK() ?: "",
        vkDataPreferencesDataSource.getPhoneVK() ?: "",
        viewModel.walletAddress,
        amount,
        BuildConfig.VK_MERCHANT_ID.toInt(),
        BuildConfig.VK_SDK_APP_ID.toInt(),
        requireFragmentManager()
      )
    } else {
      showError()
    }
    // this callback from VK Pay sdk stopped working:
    VkPayCheckout.observeCheckoutResult { result -> handleCheckoutResult(result) }
    // so we are forcing the transaction status check even before completing the payment:
    viewModel.startTransactionStatusTimer()
  }

  private fun clearVkPayCheckout() {
    VkPayCheckout.releaseResultObserver()
    VkPayCheckout.finish()
  }


  override fun onStateChanged(state: OnboardingVkPaymentStates) {
    when (state.vkTransaction) {
      is Async.Fail -> {
        showError()
      }
      else -> {}
    }
  }

  fun showError() {
    binding.loadingAuthorizationAnimation.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.activity_iab_error_message)
    binding.errorView.root.visibility = View.VISIBLE
    binding.errorTryAgainVk.visibility = View.VISIBLE
    binding.errorTryAgainVk.setOnClickListener {
      findNavController().popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
    }
    clearVkPayCheckout()
  }

  private fun showCompletedPayment() {
    binding.fragmentFirstIabTransactionCompleted.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    val bonus = args.forecastBonus.getPurchaseBonusMessage(formatter)
    if (!bonus.isNullOrEmpty()) {
      binding.fragmentFirstIabTransactionCompleted.transactionSuccessBonusText.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      binding.fragmentFirstIabTransactionCompleted.bonusSuccessLayout.visibility = View.GONE
    }
    binding.loadingAuthorizationAnimation.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.VISIBLE
    binding.fragmentFirstIabTransactionCompleted.iabFirstActivityTransactionCompleted.visibility =
      View.VISIBLE
    binding.fragmentFirstIabTransactionCompleted.lottieTransactionSuccess.playAnimation()
    clearVkPayCheckout()
  }

  override fun onSideEffect(sideEffect: OnboardingVkPaymentSideEffect) {
    when (sideEffect) {
      is OnboardingVkPaymentSideEffect.ShowError -> {
        showError()
      }

      OnboardingVkPaymentSideEffect.ShowLoading -> {}
      OnboardingVkPaymentSideEffect.ShowSuccess -> {
        showCompletedPayment()
      }

      OnboardingVkPaymentSideEffect.PaymentLinkSuccess -> {
        viewModel.transactionUid = viewModel.state.vkTransaction.value?.uid
        startVkCheckoutPay()
      }
    }
  }

  companion object {
    const val PAYMENT_DATA = "data"
  }
}
