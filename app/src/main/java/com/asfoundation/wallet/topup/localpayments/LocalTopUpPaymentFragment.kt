package com.asfoundation.wallet.topup.localpayments

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asf.wallet.databinding.LocalTopupPaymentLayoutBinding
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocalTopUpPaymentFragment : BasePageViewFragment(), LocalTopUpPaymentView {

  @Inject
  lateinit var presenter: LocalTopUpPaymentPresenter

  private lateinit var activityView: TopUpActivityView
  private var minFrame = 0
  private var maxFrame = 40

  private val binding by viewBinding(LocalTopupPaymentLayoutBinding::bind)

  companion object {

    const val PAYMENT_ID = "payment_id"
    const val PAYMENT_ICON = "payment_icon"
    const val PAYMENT_LABEL = "payment_label"
    const val PAYMENT_DATA = "data"
    const val ASYNC = "async"
    const val PACKAGE_NAME = "package_name"
    private const val ANIMATION_STEP_ONE_START_FRAME = 0
    private const val ANIMATION_STEP_TWO_START_FRAME = 80
    private const val ANIMATION_FRAME_INCREMENT = 40
    private const val BUTTON_ANIMATION_START_FRAME = 120

    fun newInstance(paymentId: String, icon: String, label: String, async: Boolean,
                    packageName: String, data: TopUpPaymentData): LocalTopUpPaymentFragment {
      val fragment = LocalTopUpPaymentFragment()
      Bundle().apply {
        putString(PAYMENT_ID, paymentId)
        putString(PAYMENT_ICON, icon)
        putString(PAYMENT_LABEL, label)
        putBoolean(ASYNC, async)
        putString(PACKAGE_NAME, packageName)
        putSerializable(PAYMENT_DATA, data)
        fragment.arguments = this
      }
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is TopUpActivityView) {
      throw IllegalStateException("Local topup payment fragment must be attached to Topup activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = LocalTopupPaymentLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun showValues(value: String, currency: String, appcValue: String,
                          selectedCurrencyType: String) {
    binding.mainValue.visibility = View.VISIBLE
    if (selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      binding.mainValue.setText(value)
      binding.mainCurrencyCode.text = currency
      binding.convertedValue.text = "$appcValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      binding.mainValue.setText(appcValue)
      binding.mainCurrencyCode.text = WalletCurrency.CREDITS.symbol
      binding.convertedValue.text = "$value $currency"
    }
  }

  override fun showError() {
    activityView.unlockRotation()
    binding.loading.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.topupPendingUserPaymentView.root.visibility = View.GONE
    binding.errorView.errorMessage.text = getString(R.string.unknown_error)
    binding.errorView.root.visibility = View.VISIBLE
  }

  override fun showNetworkError() {
    activityView.unlockRotation()
    binding.loading.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.topupPendingUserPaymentView.root.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.noNetwork.root.visibility = View.VISIBLE
  }

  override fun getSupportIconClicks() = RxView.clicks(binding.errorView.layoutSupportIcn)

  override fun getSupportLogoClicks() = RxView.clicks(binding.errorView.layoutSupportLogo)

  override fun getGotItClick() = RxView.clicks(binding.topupPendingUserPaymentView.gotItButton)

  override fun getTryAgainClick() = RxView.clicks(binding.errorView.tryAgain)

  override fun retryClick() = RxView.clicks(binding.noNetwork.retryButton)

  override fun close() = activityView.close()

  override fun showRetryAnimation() {
    binding.noNetwork.retryButton.visibility = View.INVISIBLE
    binding.noNetwork.retryAnimation.visibility = View.VISIBLE
  }

  override fun showProcessingLoading() {
    activityView.lockOrientation()
    binding.loading.visibility = View.VISIBLE
    binding.topupPendingUserPaymentView.root.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun showPendingUserPayment(paymentMethodIcon: Bitmap,
                                      paymentLabel: String) {
    activityView.unlockRotation()
    binding.loading.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.noNetwork.root.visibility = View.GONE
    binding.mainContent.visibility = View.GONE
    binding.topupPendingUserPaymentView.root.visibility = View.VISIBLE
    val placeholder = getString(R.string.async_steps_1_no_notification)
    val stepOneText = String.format(placeholder, paymentLabel)

    binding.topupPendingUserPaymentView.stepOneDesc.text = stepOneText

    binding.topupPendingUserPaymentView.topUpInProgressAnimation.updateBitmap("image_0",
        paymentMethodIcon)

    playAnimation()
  }

  private fun playAnimation() {
    binding.topupPendingUserPaymentView.topUpInProgressAnimation.setMinAndMaxFrame(minFrame,
        maxFrame)
    binding.topupPendingUserPaymentView.topUpInProgressAnimation.addAnimatorListener(object :
        Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) {
        if (minFrame == BUTTON_ANIMATION_START_FRAME) {
          binding.topupPendingUserPaymentView.topUpInProgressAnimation.cancelAnimation()
        } else {
          minFrame += ANIMATION_FRAME_INCREMENT
          maxFrame += ANIMATION_FRAME_INCREMENT
          binding.topupPendingUserPaymentView.topUpInProgressAnimation.setMinAndMaxFrame(minFrame,
              maxFrame)
          binding.topupPendingUserPaymentView.topUpInProgressAnimation.playAnimation()
        }
      }

      override fun onAnimationCancel(animation: Animator) = Unit

      override fun onAnimationStart(animation: Animator) {
        when (minFrame) {
          ANIMATION_STEP_ONE_START_FRAME -> {
            animateShow(binding.topupPendingUserPaymentView.stepOne)
            animateShow(binding.topupPendingUserPaymentView.stepOneDesc)
          }
          ANIMATION_STEP_TWO_START_FRAME -> {
            animateShow(binding.topupPendingUserPaymentView.stepTwo)
            animateShow(binding.topupPendingUserPaymentView.stepTwoDesc, binding.topupPendingUserPaymentView.gotItButton)
          }
          else -> return
        }
      }
    })
    binding.topupPendingUserPaymentView.topUpInProgressAnimation.playAnimation()
  }

  private fun animateShow(view: View, viewToAnimateInTheEnd: View? = null) {
    view.apply {
      alpha = 0.0f
      visibility = View.VISIBLE

      animate()
          .alpha(1f)
          .withEndAction {
            this.visibility = View.VISIBLE
            viewToAnimateInTheEnd?.let { animateButton(it) }
          }
          .setDuration(TimeUnit.SECONDS.toMillis(1))
          .setListener(null)
    }
  }

  private fun animateButton(view: View) {
    view.apply {
      alpha = 0.2f
      animate()
          .alpha(1f)
          .withEndAction { this.isClickable = true }
          .setDuration(TimeUnit.SECONDS.toMillis(1))
          .setListener(null)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  override fun onDestroy() {
    activityView.unlockRotation()
    super.onDestroy()
  }
}
