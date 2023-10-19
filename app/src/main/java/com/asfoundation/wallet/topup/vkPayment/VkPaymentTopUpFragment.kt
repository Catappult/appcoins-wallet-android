package com.asfoundation.wallet.topup.vkPayment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Nullable
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.VkTopupPaymentLayoutBinding
import com.asfoundation.wallet.onboarding_new_payment.local_payments.OnboardingLocalPaymentFragmentArgs
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentData
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment
import com.vk.api.sdk.VK
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.auth.main.VkClientUiInfo
import com.vk.dto.common.id.UserId
import com.vk.superapp.SuperappKit
import com.vk.superapp.SuperappKitConfig
import com.vk.superapp.core.SuperappConfig
import com.vk.superapp.vkpay.checkout.VkCheckoutResult
import com.vk.superapp.vkpay.checkout.VkCheckoutResultDisposable
import com.vk.superapp.vkpay.checkout.VkPayCheckout
import com.vk.superapp.vkpay.checkout.api.dto.model.VkMerchantInfo
import com.vk.superapp.vkpay.checkout.api.dto.model.VkTransactionInfo
import com.vk.superapp.vkpay.checkout.config.VkPayCheckoutConfig
import com.vk.superapp.vkpay.checkout.config.VkPayCheckoutConfigBuilder
import com.vk.superapp.vkpay.checkout.data.VkCheckoutUserInfo
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VkPaymentTopUpFragment : BasePageViewFragment(),
  SingleStateFragment<VkPaymentTopUpState, VkPaymentTopUpSideEffect> {

  private val viewModel: VkPaymentTopUpViewModel by viewModels()
  private val binding by lazy { VkTopupPaymentLayoutBinding.bind(requireView()) }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: VkPaymentTopUpNavigator

  private val authVkCallback = object : VkClientAuthCallback {
    override fun onAuth(authResult: AuthResult) {
      checkoutVkPay()
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
    initSuperAppKit()
    return VkTopupPaymentLayoutBinding.inflate(inflater).root

  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    if (arguments?.getSerializable(LocalTopUpPaymentFragment.PAYMENT_DATA) != null) {
      viewModel.paymentData =
        arguments?.getSerializable(LocalTopUpPaymentFragment.PAYMENT_DATA) as TopUpPaymentData
    }
    viewModel.getPaymentLink()
  }


  private fun initSuperAppKit() {
    val appName = "wallet_android_dev"
    // Укажите этот параметр и appId в файле ресурсов!
    val clientSecret = BuildConfig.VK_CLIENT_SECRET
    // Укажите иконку, которая будет отображаться в компонентах пользовательского интерфейса
    val icon = AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_launcher)!!

    val appInfo = SuperappConfig.AppInfo(
      appName,
      VK.getAppId(requireContext()).toString(),
      "1.232"
    )

    val config = activity?.let {
      SuperappKitConfig.Builder(it.application)
        .setAuthModelData(clientSecret)
        .setAuthUiManagerData(VkClientUiInfo(icon, appName))
        .setLegalInfoLinks(
          serviceUserAgreement = "https://id.vk.com/terms",
          servicePrivacyPolicy = "https://id.vk.com/privacy"
        )
        .setApplicationInfo(appInfo)
        // Получение Access token напрямую (без silentTokenExchanger)
        .setUseCodeFlow(true)
        .build()
    }

    // Инициализация SuperAppKit
    if (!SuperappKit.isInitialized()) {
      config?.let { SuperappKit.init(it) }
    }
    VkClientAuthLib.addAuthCallback(authVkCallback)
  }

  fun checkoutVkPay() {
    val hash = viewModel.state.vkTransaction.value?.hash
    val uidTransaction = viewModel.state.vkTransaction.value?.uid
    val amount = viewModel.state.vkTransaction.value?.amount
    if (hash != null && uidTransaction != null && amount != null) {
      val transaction = VkTransactionInfo(
        amount,
        uidTransaction, VkTransactionInfo.Currency.RUB
      )
      val merchantInfo = VkMerchantInfo(
        BuildConfig.VK_MERCHANT_ID.toInt(),
        hash, uidTransaction, "wallet APPC"
      )

      //This Val need to implement only in Developer Mode
      val config = if (BuildConfig.DEBUG) {
        val sandbox = VkPayCheckoutConfig.Environment.Sandbox(
          userInfo = VkCheckoutUserInfo(UserId(12345), "+1234566790"),
          useApi = false,
          mockNotCreatedVkPay = true,
          useTestMerchant = true,
          domain = VkPayCheckoutConfig.Domain.TEST
        )
        VkPayCheckoutConfigBuilder(merchantInfo).setParentAppId(BuildConfig.VK_SDK_APP_ID.toInt())
          .setEnvironment(sandbox).build()
      } else {
        VkPayCheckoutConfigBuilder(merchantInfo).setParentAppId(BuildConfig.VK_SDK_APP_ID.toInt())
          .build()
      }
      observeCheckoutResults = VkPayCheckout.observeCheckoutResult { handleCheckoutResult(it) }
      VkPayCheckout.startCheckout(requireFragmentManager(), transaction, config)
    }
  }

  private fun handleCheckoutResult(vkCheckoutResult: VkCheckoutResult) {
    if (BuildConfig.DEBUG) {
      viewModel.changeVkTransactionStatusDev()
    } else {
      viewModel.startTransactionStatusTimer()
    }
  }


  override fun onStateChanged(state: VkPaymentTopUpState) {
    when (state.vkTransaction) {
      Async.Uninitialized,
      is Async.Loading -> {

      }

      is Async.Success -> {
        if (SuperappKit.isInitialized()) {
          binding.vkFastLoginButton.performClick()
        }
      }

      is Async.Fail -> {
        showError()
      }

      else -> {}
    }
    when (state.vkTransactionDev) {
      Async.Uninitialized,
      is Async.Loading -> {
      }

      is Async.Success -> {
        viewModel.startTransactionStatusTimer()
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

  override fun onSideEffect(sideEffect: VkPaymentTopUpSideEffect) {
    when (sideEffect) {
      is VkPaymentTopUpSideEffect.ShowError -> {} //showError(message = sideEffect.message)
      VkPaymentTopUpSideEffect.ShowLoading -> {}
      VkPaymentTopUpSideEffect.ShowSuccess -> {}
    }
  }

  companion object {
    const val PAYMENT_DATA = "data"
  }
}
