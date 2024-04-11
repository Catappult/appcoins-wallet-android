package com.asfoundation.wallet.billing.mipay

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.MiPayIabLayoutBinding
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.Navigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class MiPayFragment : BasePageViewFragment(),
  SingleStateFragment<MiPayIABState, MiPayIABSideEffect> {

  private val viewModel: MiPayViewModel by viewModels()
  private val binding by lazy { MiPayIabLayoutBinding.bind(requireView()) }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: MiPayNavigator

  private lateinit var iabView: IabView
  private var navigatorIAB: Navigator? = null

  private lateinit var webViewLauncher: ActivityResultLauncher<Intent>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "MiPay payment fragment must be attached to IAB activity" }
    iabView = context
    iabView.lockRotation()
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    navigatorIAB = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    return MiPayIabLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setupTransactionCompleteAnimation()
    createResultLauncher()
    lifecycleScope.launch {
      startTransaction()
    }
  }

  private fun startTransaction() {
    if (viewModel.isFirstGetPaymentLink &&
      requireArguments().containsKey(CURRENCY_KEY) &&
      requireArguments().containsKey(TRANSACTION_DATA_KEY) &&
      requireArguments().containsKey(AMOUNT_KEY) &&
      requireArguments().containsKey(ORIGIN_KEY)
    ) {
      viewModel.getPaymentLink(
        requireArguments().getParcelable(TRANSACTION_DATA_KEY)!!,
        (requireArguments().getSerializable(AMOUNT_KEY) as BigDecimal).toString(),
        requireArguments().getString(CURRENCY_KEY)!!,
      )
      viewModel.sendPaymentStartEvent(requireArguments().getParcelable(TRANSACTION_DATA_KEY))
    } else {
      showError()
    }
  }

  private fun setupTransactionCompleteAnimation() {
    binding.successContainer.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    binding.successContainer.lottieTransactionSuccess
      .addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) = Unit
        override fun onAnimationEnd(animation: Animator) = handleCompletePurchase()
        override fun onAnimationCancel(animation: Animator) = Unit
        override fun onAnimationStart(animation: Animator) = Unit
      })
    val textDelegate = TextDelegate(binding.successContainer.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", requireArguments().getString(BONUS_KEY))
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    binding.successContainer.lottieTransactionSuccess.setTextDelegate(textDelegate)
    binding.successContainer.lottieTransactionSuccess.setFontAssetDelegate(object :
      FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }


  override fun onStateChanged(state: MiPayIABState) {
    when (state.transaction) {
      is Async.Fail -> {
        showError()
      }

      else -> {}
    }
  }

  fun showError() {
    viewModel.sendPaymentErrorEvent(
      "",
      "",
      requireArguments().getParcelable(TRANSACTION_DATA_KEY)!!
    )
    binding.loading.visibility = View.GONE
    binding.loadingHintTextView.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.activity_iab_error_message)
    binding.errorView.root.visibility = View.VISIBLE
    binding.errorTryAgain.visibility = View.VISIBLE
    binding.errorCancel.visibility = View.VISIBLE
    binding.errorTryAgain.setOnClickListener {
      iabView.navigateBack()
    }
    binding.errorCancel.setOnClickListener {
      iabView.close(bundle = Bundle())
    }
  }

  override fun onSideEffect(sideEffect: MiPayIABSideEffect) {
    when (sideEffect) {
      is MiPayIABSideEffect.ShowError -> {
        showError()
      }

      MiPayIABSideEffect.ShowLoading -> {}
      MiPayIABSideEffect.ShowSuccess -> {
        showSuccessAnimation()
      }

      MiPayIABSideEffect.PaymentLinkSuccess -> {
        showWebView()
      }

      is MiPayIABSideEffect.SendSuccessBundle -> {
        viewLifecycleOwner.lifecycleScope.launch {
          delay(1500L)
          navigatorIAB?.popView(sideEffect.bundle)
        }
      }
    }
  }

  private fun showWebView() {
    with(viewModel.state.transaction.value) {
      viewModel.transactionUid = this?.uid
      if (this?.redirectUrl != null)
        navigator.navigateToWebView(
          redirectUrl,
          webViewLauncher
        )
      else showError()
    }
  }

  private fun showSuccessAnimation() {
    viewModel.sendPaymentSuccessEvent(
      requireArguments().getParcelable(TRANSACTION_DATA_KEY)!!,
      viewModel.transactionUid!!
    )
    val bonus = requireArguments().getString(BONUS_KEY)
    if (!bonus.isNullOrEmpty()) {
      binding.successContainer.transactionSuccessBonusText.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      binding.successContainer.bonusSuccessLayout.visibility = View.GONE
    }
    binding.loading.visibility = View.GONE
    binding.loadingHintTextView.visibility = View.GONE
    binding.successContainer.iabActivityTransactionCompleted.visibility = View.VISIBLE
  }

  private fun handleCompletePurchase() {
    viewModel.getSuccessBundle(requireArguments().getParcelable(TRANSACTION_DATA_KEY))
  }

  private fun createResultLauncher() {
    webViewLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.handleWebViewResult(result)
      }
  }

  companion object {
    const val ORIGIN_KEY = "origin"
    const val TRANSACTION_DATA_KEY = "transaction_data"
    const val AMOUNT_KEY = "amount"
    const val CURRENCY_KEY = "currency"
    const val BONUS_KEY = "bonus"
  }
}
