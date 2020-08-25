package com.asfoundation.wallet.topup

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.payment.PaymentFragmentNavigator
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.error_top_up_layout.*
import kotlinx.android.synthetic.main.fragment_adyen_top_up.converted_value
import kotlinx.android.synthetic.main.fragment_adyen_top_up.loading
import kotlinx.android.synthetic.main.fragment_adyen_top_up.main_currency_code
import kotlinx.android.synthetic.main.fragment_adyen_top_up.main_value
import kotlinx.android.synthetic.main.local_topup_payment_layout.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.pending_user_payment_view.*
import kotlinx.android.synthetic.main.support_error_layout.view.*
import kotlinx.android.synthetic.main.topup_pending_user_payment_view.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocalTopUpPaymentFragment : DaggerFragment(), LocalTopUpPaymentView {

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  private lateinit var activityView: TopUpActivityView
  private lateinit var presenter: LocalTopUpPaymentPresenter
  private lateinit var navigator: PaymentFragmentNavigator
  private var minFrame = 0
  private var maxFrame = 40

  companion object {

    private const val PAYMENT_ID = "payment_id"
    private const val PAYMENT_ICON = "payment_icon"
    private const val PAYMENT_LABEL = "payment_label"
    private const val PAYMENT_DATA = "data"
    private const val ANIMATION_STEP_ONE_START_FRAME = 0
    private const val ANIMATION_STEP_TWO_START_FRAME = 80
    private const val ANIMATION_FRAME_INCREMENT = 40
    private const val BUTTON_ANIMATION_START_FRAME = 120

    fun newInstance(paymentId: String, icon: String, label: String,
                    data: TopUpPaymentData): LocalTopUpPaymentFragment {
      val fragment = LocalTopUpPaymentFragment()
      Bundle().apply {
        putString(PAYMENT_ID, paymentId)
        putString(PAYMENT_ICON, icon)
        putString(PAYMENT_LABEL, label)
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
    navigator = PaymentFragmentNavigator((activity as UriNavigator?)!!, activityView)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = LocalTopUpPaymentPresenter(this, activityView, context, localPaymentInteractor,
        topUpAnalytics, navigator, formatter, inAppPurchaseInteractor.billingMessagesMapper,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), data, paymentId,
        paymentIcon, activity!!.packageName)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_topup_payment_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun showValues(value: String, currency: String, appcValue: String) {
    main_value.visibility = View.VISIBLE
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      main_value.setText(value)
      main_currency_code.text = currency
      converted_value.text = "$appcValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      main_value.setText(appcValue)
      main_currency_code.text = WalletCurrency.CREDITS.symbol
      converted_value.text = "$value $currency"
    }
  }

  override fun showError() {
    activityView.unlockRotation()
    loading.visibility = View.GONE
    main_content.visibility = View.GONE
    no_network.visibility = View.GONE
    topup_pending_user_payment_view.visibility = View.GONE
    error_view?.error_message?.text = getString(R.string.unknown_error)
    error_view?.visibility = View.VISIBLE
  }

  override fun showNetworkError() {
    activityView.unlockRotation()
    loading.visibility = View.GONE
    main_content.visibility = View.GONE
    topup_pending_user_payment_view.visibility = View.GONE
    error_view?.visibility = View.GONE
    no_network.visibility = View.VISIBLE
  }

  override fun getSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun getSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getGotItClick() = RxView.clicks(got_it_button)

  override fun getTryAgainClick() = RxView.clicks(try_again)

  override fun retryClick() = RxView.clicks(retry_button)

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun showProcessingLoading() {
    loading.visibility = View.VISIBLE
    topup_pending_user_payment_view.visibility = View.GONE
    main_content.visibility = View.GONE
    error_view?.visibility = View.GONE
    no_network.visibility = View.GONE
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun showPendingUserPayment(paymentMethodIcon: Bitmap) {
    activityView.unlockRotation()
    loading.visibility = View.GONE
    error_view?.visibility = View.GONE
    no_network.visibility = View.GONE
    main_content.visibility = View.GONE
    topup_pending_user_payment_view.visibility = View.VISIBLE
    val placeholder = getString(R.string.async_steps_1_no_notification)
    val stepOneText = String.format(placeholder, paymentLabel)

    step_one_desc.text = stepOneText

    topup_pending_user_payment_view?.top_up_in_progress_animation?.updateBitmap("image_0",
        paymentMethodIcon)

    playAnimation()
  }

  override fun launchPerkBonusService(address: String) {
    activityView.launchPerkBonusService(address)
  }

  private fun playAnimation() {
    topup_pending_user_payment_view?.top_up_in_progress_animation?.setMinAndMaxFrame(minFrame,
        maxFrame)
    topup_pending_user_payment_view?.top_up_in_progress_animation?.addAnimatorListener(object :
        Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit

      override fun onAnimationEnd(animation: Animator?) {
        if (minFrame == BUTTON_ANIMATION_START_FRAME) {
          topup_pending_user_payment_view?.top_up_in_progress_animation?.cancelAnimation()
        } else {
          minFrame += ANIMATION_FRAME_INCREMENT
          maxFrame += ANIMATION_FRAME_INCREMENT
          topup_pending_user_payment_view?.top_up_in_progress_animation?.setMinAndMaxFrame(minFrame,
              maxFrame)
          topup_pending_user_payment_view?.top_up_in_progress_animation?.playAnimation()
        }
      }

      override fun onAnimationCancel(animation: Animator?) = Unit

      override fun onAnimationStart(animation: Animator?) {
        when (minFrame) {
          ANIMATION_STEP_ONE_START_FRAME -> {
            animateShow(step_one)
            animateShow(step_one_desc)
          }
          ANIMATION_STEP_TWO_START_FRAME -> {
            animateShow(step_two)
            animateShow(step_two_desc, got_it_button)
          }
          else -> return
        }
      }
    })
    topup_pending_user_payment_view?.top_up_in_progress_animation?.playAnimation()
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

  override fun navigateToPaymentSelection() = activityView.navigateBack()

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  override fun onDestroy() {
    activityView.unlockRotation()
    super.onDestroy()
  }

  private val paymentId: String by lazy {
    if (arguments!!.containsKey(PAYMENT_ID)) {
      arguments!!.getString(PAYMENT_ID)!!
    } else {
      throw IllegalArgumentException("payment id data not found")
    }
  }

  private val paymentIcon: String by lazy {
    if (arguments!!.containsKey(PAYMENT_ICON)) {
      arguments!!.getString(PAYMENT_ICON)!!
    } else {
      throw IllegalArgumentException("payment icon data not found")
    }
  }

  private val paymentLabel: String by lazy {
    if (arguments!!.containsKey(PAYMENT_LABEL)) {
      arguments!!.getString(PAYMENT_LABEL)!!
    } else {
      throw IllegalArgumentException("payment label data not found")
    }
  }

  private val data: TopUpPaymentData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA)!! as TopUpPaymentData
    } else {
      throw IllegalArgumentException("topup payment data not found")
    }
  }
}
