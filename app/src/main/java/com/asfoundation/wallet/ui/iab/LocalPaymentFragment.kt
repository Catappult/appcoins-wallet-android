package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.LocalPaymentView.ViewState
import com.asfoundation.wallet.ui.iab.LocalPaymentView.ViewState.*
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_iab_error.view.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.view.*
import kotlinx.android.synthetic.main.local_payment_layout.*
import kotlinx.android.synthetic.main.pending_user_payment_view.*
import kotlinx.android.synthetic.main.pending_user_payment_view.view.*
import java.math.BigDecimal
import javax.inject.Inject

class LocalPaymentFragment : DaggerFragment(), LocalPaymentView {

  companion object {

    private const val DOMAIN_KEY = "domain"
    private const val SKU_ID_KEY = "skuId"
    private const val ORIGINAL_AMOUNT_KEY = "original_amount"
    private const val CURRENCY_KEY = "currency"
    private const val PAYMENT_KEY = "payment_name"
    private const val BONUS_KEY = "bonus"
    private const val VALID_BONUS = "valid_bonus"
    private const val STATUS_KEY = "status"
    private const val TYPE_KEY = "type"
    private const val DEV_ADDRESS_KEY = "dev_address"
    private const val AMOUNT_KEY = "amount"
    private const val CALLBACK_URL = "CALLBACK_URL"
    private const val ORDER_REFERENCE = "ORDER_REFERENCE"
    private const val PAYLOAD = "PAYLOAD"

    @JvmStatic
    fun newInstance(domain: String, skudId: String?, originalAmount: String?,
                    currency: String?, bonus: String?,
                    selectedPaymentMethod: String,
                    developerAddress: String, type: String,
                    amount: BigDecimal, callbackUrl: String?,
                    orderReference: String?,
                    payload: String?, validBonus: Boolean): LocalPaymentFragment {
      val fragment = LocalPaymentFragment()
      val bundle = Bundle()
      bundle.putString(DOMAIN_KEY, domain)
      bundle.putString(SKU_ID_KEY, skudId)
      bundle.putString(ORIGINAL_AMOUNT_KEY, originalAmount)
      bundle.putString(CURRENCY_KEY, currency)
      bundle.putString(BONUS_KEY, bonus)
      bundle.putString(PAYMENT_KEY, selectedPaymentMethod)
      bundle.putString(DEV_ADDRESS_KEY, developerAddress)
      bundle.putString(TYPE_KEY, type)
      bundle.putSerializable(AMOUNT_KEY, amount)
      bundle.putString(CALLBACK_URL, callbackUrl)
      bundle.putString(ORDER_REFERENCE, orderReference)
      bundle.putString(PAYLOAD, payload)
      bundle.putBoolean(VALID_BONUS, validBonus)
      fragment.arguments = bundle
      return fragment
    }
  }

  private val domain: String by lazy {
    if (arguments!!.containsKey(DOMAIN_KEY)) {
      arguments!!.getString(DOMAIN_KEY)
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }
  private val skudId: String? by lazy {
    if (arguments!!.containsKey(SKU_ID_KEY)) {
      arguments!!.getString(SKU_ID_KEY)
    } else {
      throw IllegalArgumentException("skuId data not found")
    }
  }
  private val originalAmount: String? by lazy {
    if (arguments!!.containsKey(ORIGINAL_AMOUNT_KEY)) {
      arguments!!.getString(ORIGINAL_AMOUNT_KEY)
    } else {
      throw IllegalArgumentException("original amount data not found")
    }
  }

  private val bonus: String? by lazy {
    if (arguments!!.containsKey(BONUS_KEY)) {
      arguments!!.getString(BONUS_KEY)
    } else {
      throw IllegalArgumentException("bonus amount data not found")
    }
  }

  private val paymentId: String by lazy {
    if (arguments!!.containsKey(PAYMENT_KEY)) {
      arguments!!.getString(PAYMENT_KEY)
    } else {
      throw IllegalArgumentException("payment method data not found")
    }
  }

  private val currency: String? by lazy {
    if (arguments!!.containsKey(CURRENCY_KEY)) {
      arguments!!.getString(CURRENCY_KEY)
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val developerAddress: String by lazy {
    if (arguments!!.containsKey(DEV_ADDRESS_KEY)) {
      arguments!!.getString(DEV_ADDRESS_KEY)
    } else {
      throw IllegalArgumentException("dev address data not found")
    }
  }

  private val type: String by lazy {
    if (arguments!!.containsKey(TYPE_KEY)) {
      arguments!!.getString(TYPE_KEY)
    } else {
      throw IllegalArgumentException("type data not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val orderReference: String? by lazy {
    if (arguments!!.containsKey(ORDER_REFERENCE)) {
      arguments!!.getString(ORDER_REFERENCE)
    } else {
      throw IllegalArgumentException("dev address data not found")
    }
  }

  private val callbackUrl: String? by lazy {
    if (arguments!!.containsKey(CALLBACK_URL)) {
      arguments!!.getString(CALLBACK_URL)
    } else {
      throw IllegalArgumentException("dev address data not found")
    }
  }

  private val payload: String? by lazy {
    if (arguments!!.containsKey(PAYLOAD)) {
      arguments!!.getString(PAYLOAD)
    } else {
      throw IllegalArgumentException("dev address data not found")
    }
  }

  val validBonus: Boolean by lazy {
    if (arguments!!.containsKey(VALID_BONUS)) {
      arguments!!.getBoolean(VALID_BONUS)
    } else {
      throw IllegalArgumentException("valid bonus not found")
    }
  }

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor
  @Inject
  lateinit var analytics: LocalPaymentAnalytics
  private lateinit var iabView: IabView
  private lateinit var navigator: FragmentNavigator
  private lateinit var localPaymentPresenter: LocalPaymentPresenter
  private lateinit var status: ViewState

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    navigator = FragmentNavigator(activity as UriNavigator?, iabView)
    status = NONE
    localPaymentPresenter =
        LocalPaymentPresenter(this, originalAmount, currency, domain, skudId,
            paymentId, developerAddress, localPaymentInteractor, navigator, type, amount, analytics,
            savedInstanceState, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable(), callbackUrl, orderReference, payload)
  }


  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putSerializable(STATUS_KEY, status)
    localPaymentPresenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is IabView) {
      throw IllegalStateException("Local payment fragment must be attached to IAB activity")
    }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (validBonus) {
      complete_payment_view.lottie_transaction_success.setAnimation(
          R.raw.top_up_bonus_success_animation)
      setAnimationText()
    } else {
      complete_payment_view.lottie_transaction_success.setAnimation(R.raw.top_up_success_animation)
    }

    localPaymentPresenter.present()
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    if (savedInstanceState?.get(STATUS_KEY) != null) {
      status = savedInstanceState.get(STATUS_KEY) as ViewState
      setViewState(savedInstanceState.get(STATUS_KEY) as ViewState)
    }
    super.onViewStateRestored(savedInstanceState)
  }

  private fun setViewState(viewState: ViewState?) {
    when (viewState) {
      COMPLETED -> showCompletedPayment()
      PENDING_USER_PAYMENT -> showPendingUserPayment()
      ERROR -> showError()
      LOADING -> showProcessingLoading()
      else -> {
      }
    }
  }

  private fun setAnimationText() {
    val textDelegate = TextDelegate(complete_payment_view.lottie_transaction_success)
    textDelegate.setText("bonus_value",
        bonus)
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    complete_payment_view.lottie_transaction_success.setTextDelegate(textDelegate)
    complete_payment_view.lottie_transaction_success.setFontAssetDelegate(object :
        FontAssetDelegate() {
      override fun fetchFont(fontFamily: String?): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  override fun onDestroyView() {
    localPaymentPresenter.handleStop()
    super.onDestroyView()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_payment_layout, container, false)
  }

  override fun getOkErrorClick(): Observable<Any> {
    return RxView.clicks(error_view.activity_iab_error_ok_button)
  }


  override fun getOkBuyClick(): Observable<Any> {
    return RxView.clicks(buy_button)
  }

  override fun showProcessingLoading() {
    status = LOADING
    progress_bar.visibility = View.VISIBLE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    complete_payment_view.lottie_transaction_success.cancelAnimation()
  }

  override fun hideLoading() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    complete_payment_view.lottie_transaction_success.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
  }

  override fun showCompletedPayment() {
    status = COMPLETED
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.VISIBLE
    complete_payment_view.iab_activity_transaction_completed.visibility = View.VISIBLE
    complete_payment_view.lottie_transaction_success.playAnimation()
    pending_user_payment_view.in_progress_animation.cancelAnimation()
  }

  override fun showPendingUserPayment() {
    status = PENDING_USER_PAYMENT
    pending_user_payment_view.visibility = View.VISIBLE
    complete_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.playAnimation()
    complete_payment_view.lottie_transaction_success.cancelAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
  }

  override fun showError() {
    status = ERROR
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    complete_payment_view.lottie_transaction_success.cancelAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.VISIBLE
  }

  override fun dismissError() {
    status = NONE
    error_view.visibility = View.GONE
    iabView.showError()
  }

  override fun close() {
    status = NONE
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    complete_payment_view.lottie_transaction_success.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    complete_payment_view.visibility = View.GONE
    iabView.close(Bundle())
  }

  override fun getAnimationDuration(): Long {
    return complete_payment_view.lottie_transaction_success.duration
  }

  override fun popView(bundle: Bundle) {
    iabView.finish(bundle)
  }
}
