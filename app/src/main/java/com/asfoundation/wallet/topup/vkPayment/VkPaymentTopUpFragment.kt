package com.asfoundation.wallet.topup.vkPayment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.feature.vkpay.VkPayManager
import com.appcoins.wallet.sharedpreferences.VkDataPreferencesDataSource
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.VkTopupPaymentLayoutBinding
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
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
class VkPaymentTopUpFragment : BasePageViewFragment(),
  SingleStateFragment<VkPaymentTopUpState, VkPaymentTopUpSideEffect> {

  private val viewModel: VkPaymentTopUpViewModel by viewModels()
  private val binding by lazy { VkTopupPaymentLayoutBinding.bind(requireView()) }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: TopUpNavigator

  @Inject
  lateinit var vkPayManager: VkPayManager

  @Inject
  lateinit var vkDataPreferencesDataSource: VkDataPreferencesDataSource

  private val authVkCallback = object : VkClientAuthCallback {
    override fun onAuth(authResult: AuthResult) {
      val email = authResult.personalData?.email ?: ""
      val phone = authResult.personalData?.phone ?: ""
      vkDataPreferencesDataSource.saveAuthVk(
        accessToken = authResult.accessToken,
        email = email,
        phone = phone
      )
      viewModel.getPaymentLink(email, phone)
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
    return VkTopupPaymentLayoutBinding.inflate(inflater).root

  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    if (getSerializableExtra<TopUpPaymentData>(PAYMENT_DATA) != null) {
      viewModel.paymentData = getSerializableExtra<TopUpPaymentData>(PAYMENT_DATA)!!
    }
    val imm =
      requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
    lifecycleScope.launch {
      delay(500)  // necessary delay to ensure the superappKit is actually ready.
      if (SuperappKit.isInitialized()) {
        if (vkDataPreferencesDataSource.getAuthVk().isNullOrEmpty()) {
          binding.vkFastLoginButton.performClick()
        } else {
          viewModel.getPaymentLink(
            email = vkDataPreferencesDataSource.getEmailVK(),
            phone = vkDataPreferencesDataSource.getPhoneVK()
          )
        }
      }
    }
  }

  private fun handleCheckoutResult(vkCheckoutResult: VkCheckoutResult) {
    when (vkCheckoutResult) {
      is VkCheckoutSuccess -> {
//        viewModel.startTransactionStatusTimer()
      }

      else -> {
        showError()
      }
    }
  }

  private fun startVkCheckoutPay() {
    val hash = viewModel.state.vkTransaction.value?.hash
    val uidTransaction = viewModel.state.vkTransaction.value?.uid
    val amount = viewModel.state.vkTransaction.value?.amount
    val merchantId = viewModel.state.vkTransaction.value?.merchantId ?: "0"
    if (hash != null && uidTransaction != null && amount != null) {
      vkPayManager.checkoutVkPay(
        hash,
        uidTransaction,
        vkDataPreferencesDataSource.getEmailVK(),
        vkDataPreferencesDataSource.getPhoneVK(),
        viewModel.walletAddress,
        amount,
        merchantId.toInt(),
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


  override fun onStateChanged(state: VkPaymentTopUpState) {
    when (state.vkTransaction) {
      is Async.Fail -> {
        showError()
      }

      else -> {}
    }
  }


  private fun handleCompletePurchase() {
    val bundle = Bundle().apply {
      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
      putString(TOP_UP_AMOUNT, viewModel.paymentData.fiatValue)
      putString(TOP_UP_CURRENCY, viewModel.paymentData.fiatCurrencyCode)
      putString(BONUS, viewModel.paymentData.bonusValue.toString())
      putString(TOP_UP_CURRENCY_SYMBOL, viewModel.paymentData.fiatCurrencySymbol)
    }
    clearVkPayCheckout()
    navigator.popView(bundle)
  }

  fun showError() {
    binding.loadingAuthorizationAnimation.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.activity_iab_error_message)
    binding.errorView.root.visibility = View.VISIBLE
    clearVkPayCheckout()
    binding.errorView.tryAgain.setOnClickListener {
      navigator.navigateBack()
    }
  }

  private fun clearVkPayCheckout() {
    VkPayCheckout.releaseResultObserver()
    VkPayCheckout.finish()
  }

  override fun onSideEffect(sideEffect: VkPaymentTopUpSideEffect) {
    when (sideEffect) {
      is VkPaymentTopUpSideEffect.ShowError -> {
        showError()
      }

      VkPaymentTopUpSideEffect.ShowLoading -> {}
      VkPaymentTopUpSideEffect.ShowSuccess -> {
        handleCompletePurchase()
      }

      VkPaymentTopUpSideEffect.PaymentLinkSuccess -> {
        viewModel.transactionUid = viewModel.state.vkTransaction.value?.uid
        startVkCheckoutPay()
      }
    }
  }

  companion object {
    const val PAYMENT_DATA = "data"
    internal const val TOP_UP_AMOUNT = "top_up_amount"
    internal const val TOP_UP_CURRENCY = "currency"
    internal const val TOP_UP_CURRENCY_SYMBOL = "currency_symbol"
    internal const val BONUS = "bonus"
  }
}
