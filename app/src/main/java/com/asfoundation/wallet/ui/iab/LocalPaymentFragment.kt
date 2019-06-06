package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.view.rx.RxAlertDialog
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_iab_error.*
import kotlinx.android.synthetic.main.local_payment_layout.*
import javax.inject.Inject

class LocalPaymentFragment : DaggerFragment(), LocalPaymentView {

  companion object {

    private const val DOMAIN_KEY = "domain"
    private const val SKU_ID_KEY = "skuId"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val PAYMENT_KEY = "payment_name"

    @JvmStatic
    fun newInstance(domain: String, skudId: String, amount: String?,
                    currency: String?, selectedPaymentMethod: String): LocalPaymentFragment {
      val fragment = LocalPaymentFragment()
      val bundle = Bundle()
      bundle.putString(DOMAIN_KEY, domain)
      bundle.putString(SKU_ID_KEY, skudId)
      bundle.putString(AMOUNT_KEY, amount)
      bundle.putString(CURRENCY_KEY, currency)
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
  private val skudId: String by lazy {
    if (arguments!!.containsKey(SKU_ID_KEY)) {
      arguments!!.getString(SKU_ID_KEY)
    } else {
      throw IllegalArgumentException("skuId data not found")
    }
  }
  private val amount: String? by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getString(AMOUNT_KEY)
    } else {
      throw IllegalArgumentException("amount data not found")
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

  private val genericErrorDialog: RxAlertDialog by lazy {
    RxAlertDialog.Builder(context)
        .setMessage(R.string.unknown_error)
        .setPositiveButton(R.string.ok)
        .build()
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
        LocalPaymentPresenter(this, amount, currency, domain, skudId,
            paymentId, localPaymentInteractor, navigator, CompositeDisposable())
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
    return RxView.clicks(activity_iab_error_ok_button)
  }

  override fun showProcessingLoading() {
    local_payment_layout.visibility = View.VISIBLE
    progress_bar.visibility = View.VISIBLE
    error_view.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    pending_user_payment_view.cancelAnimation()
  }

  override fun hideLoading() {
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
    pending_user_payment_view.cancelAnimation()
    pending_user_payment_view.visibility = View.GONE
    local_payment_layout.visibility = View.INVISIBLE
  }

  override fun showPendingUserPayment() {
    local_payment_layout.visibility = View.VISIBLE
    pending_user_payment_view.visibility = View.VISIBLE
    pending_user_payment_view.playAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.GONE
  }

  override fun showError() {
    local_payment_layout.visibility = View.GONE
    pending_user_payment_view.visibility = View.GONE
    pending_user_payment_view.cancelAnimation()
    progress_bar.visibility = View.GONE
    error_view.visibility = View.VISIBLE
  }

  override fun dismissError() {
    error_view.visibility = View.GONE
    iabView.close(Bundle())
  }
}
