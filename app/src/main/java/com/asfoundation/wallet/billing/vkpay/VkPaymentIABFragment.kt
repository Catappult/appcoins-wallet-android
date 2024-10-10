package com.asfoundation.wallet.billing.vkpay

import android.animation.Animator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.extensions.getParcelableExtra
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.feature.vkpay.VkPayManager
import com.appcoins.wallet.sharedpreferences.VkDataPreferencesDataSource
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.VkPaymentIabLayoutBinding
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.Navigator
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.auth.oauth.VkOAuthConnectionResult
import com.vk.superapp.SuperappKit
import com.vk.superapp.vkpay.checkout.VkCheckoutFailed
import com.vk.superapp.vkpay.checkout.VkCheckoutResult
import com.vk.superapp.vkpay.checkout.VkCheckoutSuccess
import com.vk.superapp.vkpay.checkout.VkPayCheckout
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class VkPaymentIABFragment : BasePageViewFragment(),
  SingleStateFragment<VkPaymentIABState, VkPaymentIABSideEffect> {

  private val viewModel: VkPaymentIABViewModel by viewModels()
  private val binding by lazy { VkPaymentIabLayoutBinding.bind(requireView()) }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var vkDataPreferencesDataSource: VkDataPreferencesDataSource

  @Inject
  lateinit var vkPayManager: VkPayManager

  private lateinit var iabView: IabView
  private var navigatorIAB: Navigator? = null

  private val authVkCallback = object : VkClientAuthCallback {
    override fun onAuth(authResult: AuthResult) {
      val email = authResult.personalData?.email ?: ""
      val phone = authResult.personalData?.phone ?: ""
      vkDataPreferencesDataSource.saveAuthVk(
        accessToken = authResult.accessToken,
        email = email,
        phone = phone
      )
      viewModel.hasVkUserAuthenticated = true
      startTransaction(email, phone)
    }

    override fun onCancel() {
      super.onCancel()
      showError()
    }

    override fun onOAuthConnectResult(result: VkOAuthConnectionResult) {
      super.onOAuthConnectResult(result)
      when (result) {
        is VkOAuthConnectionResult.Error ->  {
          iabView.navigateBack()
        }
        else -> {
          //Do nothing
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "VkPay payment fragment must be attached to IAB activity" }
    iabView = context
    iabView.lockRotation()
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
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
    navigatorIAB = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    return VkPaymentIabLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    clearVkPayCheckout()
    setupTransactionCompleteAnimation()
    binding.loading.visibility = View.VISIBLE
    lifecycleScope.launch {
      delay(500)  // necessary delay to ensure the superappKit is actually ready.
      if (SuperappKit.isInitialized()) {
        if (vkDataPreferencesDataSource.getAuthVk().isNullOrEmpty()) {
          binding.vkFastLoginButton.performClick()
        } else {
          startTransaction(
            email = vkDataPreferencesDataSource.getEmailVK(),
            phone = vkDataPreferencesDataSource.getPhoneVK()
          )
        }
      }
    }
  }

  private fun startTransaction(email: String, phone: String) {
    if (viewModel.isFirstGetPaymentLink &&
      requireArguments().containsKey(CURRENCY_KEY) &&
      requireArguments().containsKey(TRANSACTION_DATA_KEY) &&
      requireArguments().containsKey(AMOUNT_KEY) &&
      requireArguments().containsKey(ORIGIN_KEY)
    ) {
      viewModel.getPaymentLink(
        getParcelableExtra<TransactionBuilder>(TRANSACTION_DATA_KEY)!!,
        getSerializableExtra<BigDecimal>(AMOUNT_KEY).toString(),
        requireArguments().getString(CURRENCY_KEY)!!,
        requireArguments().getString(ORIGIN_KEY)!!,
        email,
        phone
      )
      viewModel.sendPaymentStartEvent(getParcelableExtra(TRANSACTION_DATA_KEY))
    } else {
      showError()
    }
  }

  private fun setupTransactionCompleteAnimation() {
    binding.successContainerVk.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    binding.successContainerVk.lottieTransactionSuccess
      .addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) = Unit
        override fun onAnimationEnd(animation: Animator) = handleCompletePurchase()
        override fun onAnimationCancel(animation: Animator) = Unit
        override fun onAnimationStart(animation: Animator) = Unit
      })
    val textDelegate = TextDelegate(binding.successContainerVk.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", requireArguments().getString(BONUS_KEY))
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    binding.successContainerVk.lottieTransactionSuccess.setTextDelegate(textDelegate)
    binding.successContainerVk.lottieTransactionSuccess.setFontAssetDelegate(object :
      FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private fun handleCheckoutResult(vkCheckoutResult: VkCheckoutResult) {
    when (vkCheckoutResult) {
      is VkCheckoutSuccess -> {
        viewModel.startTransactionStatusTimer()
      }

      is VkCheckoutFailed -> {
        iabView.navigateBack()
      }
    }
  }

  private fun startVkCheckoutPay() {
    val hash = viewModel.transactionVkData.value?.hash
    val uidTransaction = viewModel.transactionVkData.value?.uid
    val amount = viewModel.transactionVkData.value?.amount
    val merchantId = viewModel.transactionVkData.value?.merchantId ?: "0"
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
        requireActivity().supportFragmentManager
      )
      viewModel.hasVkPayAlreadyOpened = true
    } else {
      showError()
    }
    VkPayCheckout.observeCheckoutResult { result -> handleCheckoutResult(result) }
  }


  override fun onStateChanged(state: VkPaymentIABState) {
    when (state.vkTransaction) {
      is Async.Fail -> {
        showError()
      }

      else -> {}
    }
  }

  fun showError() {
    viewModel.sendPaymentErrorEvent(
      errorCode = "",
      errorMessage = "",
      transactionBuilder = getParcelableExtra<TransactionBuilder>(TRANSACTION_DATA_KEY)!!
    )
    binding.loading.visibility = View.GONE
    binding.loadingHintTextView.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.activity_iab_error_message)
    binding.errorView.root.visibility = View.VISIBLE
    binding.errorTryAgainVk.visibility = View.VISIBLE
    binding.errorCancelVk.visibility = View.VISIBLE
    clearVkPayCheckout()
    binding.errorTryAgainVk.setOnClickListener {
      iabView.navigateBack()
    }
    binding.errorCancelVk.setOnClickListener {
      iabView.close(bundle = Bundle())
    }
  }

  override fun onSideEffect(sideEffect: VkPaymentIABSideEffect) {
    when (sideEffect) {
      is VkPaymentIABSideEffect.ShowError -> {
        showError()
      }

      VkPaymentIABSideEffect.ShowLoading -> {
        binding.loading.visibility = View.VISIBLE
      }

      VkPaymentIABSideEffect.ShowSuccess -> {
        showSuccessAnimation()
      }

      VkPaymentIABSideEffect.PaymentLinkSuccess -> {
        viewModel.transactionUid = viewModel.state.vkTransaction.value?.uid
        startVkCheckoutPay()
      }

      is VkPaymentIABSideEffect.SendSuccessBundle -> {
        viewLifecycleOwner.lifecycleScope.launch {
          delay(1500L)
          navigatorIAB?.popView(sideEffect.bundle)
        }
      }
    }
  }

  private fun clearVkPayCheckout() {
    VkPayCheckout.releaseResultObserver()
    VkPayCheckout.finish()
  }


  private fun showSuccessAnimation() {
    viewModel.sendPaymentSuccessEvent(
      transactionBuilder = getParcelableExtra<TransactionBuilder>(TRANSACTION_DATA_KEY)!!,
      txId = viewModel.transactionUid!!
    )
    val bonus = requireArguments().getString(BONUS_KEY)
    if (!bonus.isNullOrEmpty()) {
      binding.successContainerVk.transactionSuccessBonusText.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      binding.successContainerVk.bonusSuccessLayout.visibility = View.GONE
    }
    binding.loading.visibility = View.GONE
    binding.loadingHintTextView.visibility = View.GONE
    binding.successContainerVk.iabActivityTransactionCompleted.visibility = View.VISIBLE
    clearVkPayCheckout()
  }

  private fun handleCompletePurchase() {
    viewModel.getSuccessBundle(getParcelableExtra(TRANSACTION_DATA_KEY))
  }

  companion object {
    const val PAYMENT_TYPE_KEY = "payment_type"
    const val ORIGIN_KEY = "origin"
    const val TRANSACTION_DATA_KEY = "transaction_data"
    const val AMOUNT_KEY = "amount"
    const val CURRENCY_KEY = "currency"
    const val BONUS_KEY = "bonus"
    const val IS_SKILLS = "is_skills"
    const val FREQUENCY = "frequency"
    const val SKU_DESCRIPTION = "sku_description"
  }
}
