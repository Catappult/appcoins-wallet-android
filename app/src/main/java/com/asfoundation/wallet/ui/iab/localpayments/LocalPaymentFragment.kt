package com.asfoundation.wallet.ui.iab.localpayments

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.asf.wallet.databinding.LocalPaymentLayoutBinding
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentView.ViewState
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentView.ViewState.*
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocalPaymentFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), LocalPaymentView {

  @Inject
  lateinit var localPaymentPresenter: LocalPaymentPresenter

  private lateinit var iabView: IabView
  private lateinit var status: ViewState
  private var errorMessage = R.string.activity_iab_error_message
  private var minFrame = 0
  private var maxFrame = 40

  private val binding by lazy { LocalPaymentLayoutBinding.bind(requireView()) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    status = NONE
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putSerializable(STATUS_KEY, status)
    outState.putInt(ERROR_MESSAGE_KEY, errorMessage)
    localPaymentPresenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) {
      throw IllegalStateException("Local payment fragment must be attached to IAB activity")
    }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    localPaymentPresenter.present(savedInstanceState)
  }

  override fun setupUi(bonus: String?) {
    if (bonus?.isNotBlank() == true) {
      binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.setAnimation(
          R.raw.top_up_bonus_success_animation)
      setAnimationText(bonus)
    } else {
      binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.setAnimation(R.raw.top_up_success_animation)
    }

    iabView.disableBack()
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    if (savedInstanceState?.get(STATUS_KEY) != null) {
      status = savedInstanceState.get(STATUS_KEY) as ViewState
      errorMessage = savedInstanceState.getInt(ERROR_MESSAGE_KEY, errorMessage)
      setViewState()
    }
    super.onViewStateRestored(savedInstanceState)
  }

  private fun setViewState() = when (status) {
    COMPLETED -> showCompletedPayment()
    PENDING_USER_PAYMENT -> localPaymentPresenter.preparePendingUserPayment()
    ERROR -> showError()
    LOADING -> showProcessingLoading()
    else -> Unit
  }

  private fun setAnimationText(bonus: String?) {
    val textDelegate = TextDelegate(binding.fragmentIabTransactionCompleted.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.setTextDelegate(textDelegate)
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.setFontAssetDelegate(object :
        FontAssetDelegate() {
      override fun fetchFont(fontFamily: String?) =
          Typeface.create("sans-serif-medium", Typeface.BOLD)
    })
  }

  override fun onDestroyView() {
    localPaymentPresenter.handleStop()
    super.onDestroyView()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = LocalPaymentLayoutBinding.inflate(inflater).root

  override fun getErrorDismissClick() = RxView.clicks(binding.errorView.errorDismiss)

  override fun getSupportLogoClicks() = RxView.clicks(binding.errorView.genericErrorLayout.layoutSupportLogo)

  override fun getSupportIconClicks() = RxView.clicks(binding.errorView.genericErrorLayout.layoutSupportIcn)

  override fun getGotItClick() = RxView.clicks(binding.pendingUserPaymentView.gotItButton)

  override fun showVerification() = iabView.showVerification(false)

  override fun showProcessingLoading() {
    status = LOADING
    binding.progressBar.visibility = View.VISIBLE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
  }

  override fun hideLoading() {
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
  }

  override fun showCompletedPayment() {
    status = COMPLETED
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.playAnimation()
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
  }

  override fun showPendingUserPayment(paymentMethodLabel: String?, paymentMethodIcon: Bitmap,
                                      applicationIcon: Bitmap) {
    status = PENDING_USER_PAYMENT
    binding.errorView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
    binding.progressBar.visibility = View.GONE
    binding.pendingUserPaymentView.root.visibility = View.VISIBLE

    val placeholder = getString(R.string.async_steps_1_no_notification)
    val stepOneText = String.format(placeholder, paymentMethodLabel)

    binding.pendingUserPaymentView.stepOneDesc.text = stepOneText

    binding.pendingUserPaymentView.inProgressAnimation.updateBitmap("image_0", paymentMethodIcon)
    binding.pendingUserPaymentView.inProgressAnimation.updateBitmap("image_1", applicationIcon)

    playAnimation()
  }

  override fun showError(message: Int?) {
    status = ERROR
    binding.errorView.genericErrorLayout.errorMessage.text = getString(R.string.ok)
    message?.let { errorMessage = it }
    binding.errorView.genericErrorLayout.errorMessage.text = getString(message ?: errorMessage)
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.VISIBLE
  }

  override fun dismissError() {
    status = NONE
    binding.errorView.root.visibility = View.GONE
    iabView.finishWithError()
  }

  override fun close() {
    status = NONE
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
    iabView.close(Bundle())
  }

  override fun getAnimationDuration() = binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.duration

  override fun popView(bundle: Bundle, paymentId: String) {
    bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)
    iabView.finish(bundle)
  }

  override fun lockRotation() {
    iabView.lockRotation()
  }

  private fun playAnimation() {
    binding.pendingUserPaymentView.inProgressAnimation.setMinAndMaxFrame(minFrame, maxFrame)
    binding.pendingUserPaymentView.inProgressAnimation.addAnimatorListener(object :
        Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) {
        if (maxFrame == LAST_ANIMATION_FRAME) {
          binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        }
        if (minFrame == BUTTON_ANIMATION_START_FRAME) {
          minFrame += LAST_ANIMATION_FRAME_INCREMENT
          maxFrame += LAST_ANIMATION_FRAME_INCREMENT
          binding.pendingUserPaymentView.inProgressAnimation.setMinAndMaxFrame(minFrame, maxFrame)
          binding.pendingUserPaymentView.inProgressAnimation.playAnimation()
        } else {
          minFrame += MID_ANIMATION_FRAME_INCREMENT
          maxFrame += MID_ANIMATION_FRAME_INCREMENT
          binding.pendingUserPaymentView.inProgressAnimation.setMinAndMaxFrame(minFrame, maxFrame)
          binding.pendingUserPaymentView.inProgressAnimation.playAnimation()
        }
      }
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) {
        when (minFrame) {
          ANIMATION_STEP_ONE_START_FRAME -> {
            animateShow(binding.pendingUserPaymentView.stepOne)
            animateShow(binding.pendingUserPaymentView.stepOneDesc)
          }
          ANIMATION_STEP_TWO_START_FRAME -> {
            animateShow(binding.pendingUserPaymentView.stepTwo)
            animateShow(binding.pendingUserPaymentView.stepTwoDesc)
          }
          BUTTON_ANIMATION_START_FRAME -> animateButton(binding.pendingUserPaymentView.gotItButton)
          else -> return
        }
      }
    })
    binding.pendingUserPaymentView.inProgressAnimation.playAnimation()
  }

  private fun animateShow(view: View) {
    view.apply {
      alpha = 0.0f
      visibility = View.VISIBLE
      lockRotation()

      animate()
          .alpha(1f)
          .withEndAction { this.visibility = View.VISIBLE }
          .setDuration(TimeUnit.SECONDS.toMillis(1))
          .setListener(null)
    }
  }

  private fun animateButton(view: View) {
    view.apply {
      alpha = 0.2f

      animate()
          .alpha(1f)
          .withEndAction {
            this.isClickable = true
            iabView.enableBack()
          }
          .setDuration(TimeUnit.SECONDS.toMillis(1))
          .setListener(null)
    }
  }

  companion object {

    const val DOMAIN_KEY = "domain"
    const val SKU_ID_KEY = "skuId"
    const val ORIGINAL_AMOUNT_KEY = "original_amount"
    const val CURRENCY_KEY = "currency"
    const val PAYMENT_KEY = "payment_name"
    const val BONUS_KEY = "bonus"
    const val STATUS_KEY = "status"
    const val ERROR_MESSAGE_KEY = "error_message"
    const val TYPE_KEY = "type"
    const val DEV_ADDRESS_KEY = "dev_address"
    const val AMOUNT_KEY = "amount"
    const val CALLBACK_URL = "CALLBACK_URL"
    const val ORDER_REFERENCE = "ORDER_REFERENCE"
    const val PAYLOAD = "PAYLOAD"
    const val ORIGIN = "ORIGIN"
    const val PAYMENT_METHOD_URL = "payment_method_url"
    const val PAYMENT_METHOD_LABEL = "payment_method_label"
    const val ASYNC = "async"
    const val REFERRER_URL = "referrer_url"
    const val GAMIFICATION_LEVEL = "gamification_level"
    private const val ANIMATION_STEP_ONE_START_FRAME = 0
    private const val ANIMATION_STEP_TWO_START_FRAME = 80
    private const val MID_ANIMATION_FRAME_INCREMENT = 40
    private const val LAST_ANIMATION_FRAME_INCREMENT = 30
    private const val BUTTON_ANIMATION_START_FRAME = 120
    private const val LAST_ANIMATION_FRAME = 150

    @JvmStatic
    fun newInstance(domain: String, skudId: String?, originalAmount: String?, currency: String?,
                    bonus: String?, selectedPaymentMethod: String, developerAddress: String,
                    type: String, amount: BigDecimal, callbackUrl: String?, orderReference: String?,
                    payload: String?, origin: String?, paymentMethodIconUrl: String,
                    paymentMethodLabel: String, async: Boolean, referralUrl: String?,
                    gamificationLevel: Int): LocalPaymentFragment {
      return LocalPaymentFragment()
          .apply {
            arguments = Bundle().apply {
              putString(DOMAIN_KEY, domain)
              putString(SKU_ID_KEY, skudId)
              putString(ORIGINAL_AMOUNT_KEY, originalAmount)
              putString(CURRENCY_KEY, currency)
              putString(BONUS_KEY, bonus)
              putString(PAYMENT_KEY, selectedPaymentMethod)
              putString(DEV_ADDRESS_KEY, developerAddress)
              putString(TYPE_KEY, type)
              putSerializable(AMOUNT_KEY, amount)
              putString(CALLBACK_URL, callbackUrl)
              putString(ORDER_REFERENCE, orderReference)
              putString(PAYLOAD, payload)
              putString(ORIGIN, origin)
              putString(PAYMENT_METHOD_URL, paymentMethodIconUrl)
              putString(PAYMENT_METHOD_LABEL, paymentMethodLabel)
              putBoolean(ASYNC, async)
              putString(REFERRER_URL, referralUrl)
              putInt(GAMIFICATION_LEVEL, gamificationLevel)
            }
          }
    }
  }
}
