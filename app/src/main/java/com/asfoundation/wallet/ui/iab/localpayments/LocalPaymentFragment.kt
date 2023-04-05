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
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocalPaymentFragment : BasePageViewFragment(), LocalPaymentView {

  @Inject
  lateinit var localPaymentPresenter: LocalPaymentPresenter

  private lateinit var iabView: IabView
  private lateinit var status: ViewState
  private var errorMessage = R.string.activity_iab_error_message
  private var minFrame = 0
  private var maxFrame = 40

  private var _binding: LocalPaymentLayoutBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // local_payment_layout.xml
  private val complete_payment_view get() = binding.completePaymentView
  private val pending_user_payment_view get() = binding.pendingUserPaymentView.root
  private val progress_bar get() = binding.progressBar
  private val error_view get() = binding.errorView.root

  // fragment_iab_transaction_completed.xml
  private val iab_activity_transaction_completed get() = binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted
  private val lottie_transaction_success get() = binding.fragmentIabTransactionCompleted.lottieTransactionSuccess

  // pending_user_payment_view.xml
  private val got_it_button get() = binding.pendingUserPaymentView.gotItButton
  private val in_progress_animation get() = binding.pendingUserPaymentView.inProgressAnimation
  private val step_one get() = binding.pendingUserPaymentView.stepOne
  private val step_one_desc get() = binding.pendingUserPaymentView.stepOneDesc
  private val step_two get() = binding.pendingUserPaymentView.stepTwo
  private val step_two_desc get() = binding.pendingUserPaymentView.stepTwoDesc
  private val next_steps_title get() = binding.pendingUserPaymentView.nextStepsTitle

  // iab_error_layout.xml
  private val error_dismiss get() = binding.errorView.errorDismiss

  // support_error_layout.xml
  private val layout_support_logo get() = binding.errorView.genericErrorLayout.layoutSupportLogo
  private val layout_support_icn get() = binding.errorView.genericErrorLayout.layoutSupportIcn
  private val error_message get() = binding.errorView.genericErrorLayout.errorMessage

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
      lottie_transaction_success?.setAnimation(
          R.raw.top_up_bonus_success_animation)
      setAnimationText(bonus)
    } else {
      lottie_transaction_success?.setAnimation(R.raw.top_up_success_animation)
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
    val textDelegate = TextDelegate(lottie_transaction_success)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    lottie_transaction_success.setTextDelegate(textDelegate)
    lottie_transaction_success.setFontAssetDelegate(object :
        FontAssetDelegate() {
      override fun fetchFont(fontFamily: String?) =
          Typeface.create("sans-serif-medium", Typeface.BOLD)
    })
  }

  override fun onDestroyView() {
    localPaymentPresenter.handleStop()
    super.onDestroyView()
    _binding = null
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = LocalPaymentLayoutBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun getErrorDismissClick() = RxView.clicks(error_dismiss)

  override fun getSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun getGotItClick() = RxView.clicks(got_it_button)

  override fun showVerification() = iabView.showVerification(false)

  override fun showProcessingLoading() {
    status = LOADING
    progress_bar.visibility = View.VISIBLE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    in_progress_animation.cancelAnimation()
    lottie_transaction_success.cancelAnimation()
  }

  override fun hideLoading() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    in_progress_animation.cancelAnimation()
    lottie_transaction_success.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
  }

  override fun showCompletedPayment() {
    status = COMPLETED
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.VISIBLE
    iab_activity_transaction_completed.visibility = View.VISIBLE
    lottie_transaction_success.playAnimation()
    in_progress_animation.cancelAnimation()
  }

  override fun showPendingUserPayment(paymentMethodLabel: String?, paymentMethodIcon: Bitmap,
                                      applicationIcon: Bitmap) {
    status = PENDING_USER_PAYMENT
    error_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
    progress_bar.visibility = View.GONE
    pending_user_payment_view.visibility = View.VISIBLE

    val placeholder = getString(R.string.async_steps_1_no_notification)
    val stepOneText = String.format(placeholder, paymentMethodLabel)

    step_one_desc.text = stepOneText

    in_progress_animation.updateBitmap("image_0", paymentMethodIcon)
    in_progress_animation.updateBitmap("image_1", applicationIcon)

    playAnimation()
  }

  override fun showError(message: Int?) {
    status = ERROR
    error_message.text = getString(R.string.ok)
    message?.let { errorMessage = it }
    error_message.text = getString(message ?: errorMessage)
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
    in_progress_animation.cancelAnimation()
    lottie_transaction_success.cancelAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.VISIBLE
  }

  override fun dismissError() {
    status = NONE
    error_view.visibility = View.GONE
    iabView.finishWithError()
  }

  override fun close() {
    status = NONE
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    in_progress_animation.cancelAnimation()
    lottie_transaction_success.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
    iabView.close(Bundle())
  }

  override fun getAnimationDuration() = lottie_transaction_success.duration

  override fun popView(bundle: Bundle, paymentId: String) {
    bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)
    iabView.finish(bundle)
  }

  override fun lockRotation() {
    iabView.lockRotation()
  }

  private fun playAnimation() {
    in_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
    in_progress_animation.addAnimatorListener(object :
        Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) {
        if (maxFrame == LAST_ANIMATION_FRAME) {
          in_progress_animation.cancelAnimation()
        }
        if (minFrame == BUTTON_ANIMATION_START_FRAME) {
          minFrame += LAST_ANIMATION_FRAME_INCREMENT
          maxFrame += LAST_ANIMATION_FRAME_INCREMENT
          in_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
          in_progress_animation.playAnimation()
        } else {
          minFrame += MID_ANIMATION_FRAME_INCREMENT
          maxFrame += MID_ANIMATION_FRAME_INCREMENT
          in_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
          in_progress_animation.playAnimation()
        }
      }
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) {
        when (minFrame) {
          ANIMATION_STEP_ONE_START_FRAME -> {
            animateShow(step_one)
            animateShow(step_one_desc)
          }
          ANIMATION_STEP_TWO_START_FRAME -> {
            animateShow(step_two)
            animateShow(step_two_desc)
          }
          BUTTON_ANIMATION_START_FRAME -> animateButton(got_it_button)
          else -> return
        }
      }
    })
    in_progress_animation?.playAnimation()
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
