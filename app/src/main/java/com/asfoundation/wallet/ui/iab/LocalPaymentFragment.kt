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
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.completed_payment_view.view.*
import kotlinx.android.synthetic.main.fragment_iab_error.view.*
import kotlinx.android.synthetic.main.local_payment_layout.*
import kotlinx.android.synthetic.main.pending_user_payment_view.*
import kotlinx.android.synthetic.main.pending_user_payment_view.view.*
import javax.inject.Inject

class LocalPaymentFragment : DaggerFragment(), LocalPaymentView {

  companion object {

    private const val DOMAIN_KEY = "domain"
    private const val SKU_ID_KEY = "skuId"
    private const val ORIGINAL_AMOUNT_KEY = "original_amount"
    private const val CURRENCY_KEY = "currency"
    private const val PAYMENT_KEY = "payment_name"
    private const val BONUS_KEY = "bonus"

    @JvmStatic
    fun newInstance(domain: String, skudId: String?, originalAmount: String?,
                    currency: String?, bonus: String?,
                    selectedPaymentMethod: String): LocalPaymentFragment {
      val fragment = LocalPaymentFragment()
      val bundle = Bundle()
      bundle.putString(DOMAIN_KEY, domain)
      bundle.putString(SKU_ID_KEY, skudId)
      bundle.putString(ORIGINAL_AMOUNT_KEY, originalAmount)
      bundle.putString(CURRENCY_KEY, currency)
      bundle.putString(BONUS_KEY, bonus)
      bundle.putString(PAYMENT_KEY, selectedPaymentMethod)
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

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor

  private lateinit var iabView: IabView
  private lateinit var navigator: FragmentNavigator
  private lateinit var localPaymentPresenter: LocalPaymentPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    navigator = FragmentNavigator(activity as UriNavigator?, iabView)
    localPaymentPresenter =
        LocalPaymentPresenter(this, originalAmount, currency, domain, skudId,
            paymentId, localPaymentInteractor, navigator,
            savedInstanceState, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable())
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    localPaymentPresenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is IabView) {
      throw IllegalStateException("Regular buy fragment must be attached to IAB activity")
    }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val textDelegate = TextDelegate(completed_payment_view.completed_animation)
    textDelegate.setText("bonus_value",
        bonus)
    textDelegate.setText("bonus_received",
        "Bonus Received") //TODO change when we have string
    completed_payment_view.completed_animation.setTextDelegate(textDelegate)
    completed_payment_view.completed_animation.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String?): Typeface {
        return Typeface.create("sans-serif-black", Typeface.BOLD)
      }
    })
    localPaymentPresenter.present()
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
    progress_bar.visibility = View.VISIBLE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
  }

  override fun hideLoading() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
  }

  override fun showCompletedPayment() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    completed_payment_view.visibility = View.VISIBLE
    completed_payment_view.completed_animation.playAnimation()
  }

  override fun showPendingUserPayment() {
    pending_user_payment_view.visibility = View.VISIBLE
    pending_user_payment_view.in_progress_animation.playAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
  }

  override fun showError() {
    pending_user_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    completed_payment_view.visibility = View.GONE
    completed_payment_view.completed_animation.playAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.VISIBLE
  }

  override fun dismissError() {
    error_view.visibility = View.GONE
    iabView.showError()
  }

  override fun close() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    completed_payment_view.visibility = View.GONE
    pending_user_payment_view.in_progress_animation.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    iabView.close(Bundle())
  }
}
