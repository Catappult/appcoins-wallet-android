package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.Adyen
import com.asfoundation.wallet.topup.TopUpFragmentPresenter.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodAdapter
import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_top_up.*
import javax.inject.Inject


class TopUpFragment : DaggerFragment(), TopUpFragmentView {
  @Inject
  lateinit var interactor: TopUpInteractor

  private lateinit var adapter: TopUpPaymentMethodAdapter
  private lateinit var presenter: TopUpFragmentPresenter
  private lateinit var paymentMethodClick: PublishRelay<String>
  private lateinit var fragmentContainer: ViewGroup
  private var topUpActivityView: TopUpActivityView? = null

  companion object {
    private const val PARAM_APP_PACKAGE = "APP_PACKAGE"

    @JvmStatic
    fun newInstance(packageName: String): TopUpFragment {
      val bundle = Bundle()
      bundle.putString(PARAM_APP_PACKAGE, packageName)
      val fragment = TopUpFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  val appPackage: String
    get() {
      if (arguments!!.containsKey(PARAM_APP_PACKAGE)) {
        return arguments!!.getString(PARAM_APP_PACKAGE)
      }
      throw IllegalArgumentException("application package name data not found")
    }

  override fun onDetach() {
    super.onDetach()
    topUpActivityView = null
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    topUpActivityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentMethodClick = PublishRelay.create()
    presenter =
        TopUpFragmentPresenter(this, topUpActivityView, interactor, AndroidSchedulers.mainThread(),
            Schedulers.io(), appPackage)

  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUiElements(data: TopUpData) {
    updateCurrencyData(data.currency)
    main_value.isEnabled = true

    adapter = TopUpPaymentMethodAdapter(data.methods, paymentMethodClick)
    payment_methods.adapter = adapter
    payment_methods.layoutManager = LinearLayoutManager(context)
    payment_methods.visibility = View.VISIBLE
    swap_value_button.isEnabled = true
    swap_value_button.visibility = View.VISIBLE
    swap_value_lable.visibility = View.VISIBLE
  }

  override fun getChangeCurrencyClick(): Observable<Any> {
    return RxView.clicks(swap_value_button)
  }

  override fun getEditTextChanges(): InitialValueObservable<TextViewAfterTextChangeEvent> {
    return RxTextView.afterTextChangeEvents(main_value)
  }

  override fun getPaymentMethodClick(): Observable<String> {
    return paymentMethodClick
  }

  override fun getEditTextFocusChanges(): InitialValueObservable<Boolean> {
    return RxView.focusChanges(main_value)
  }

  override fun getNextClick(): Observable<Any> {
    return RxView.clicks(button)
  }


  override fun updateCurrencyData(data: CurrencyData) {
    currency_code.text = data.fiatCurrencyCode
    if (data.fiatValue != DEFAULT_VALUE && main_value.text.toString() != data.fiatValue) {
      main_value.setText(data.fiatValue)
      main_value.setSelection(main_value.text.length)
    }
    main_value_currency.text = data.fiatCurrencySymbol

    swap_value_lable.text = data.appcCode
    converted_value.text = "${data.appcValue} ${data.appcSymbol}"
  }

  override fun setNextButtonState(enabled: Boolean) {
    Log.e(this.javaClass.simpleName, "setNextButtonState: $enabled")
    button.isEnabled = enabled
  }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
  }

  override fun showLoading() {
    fragment_braintree_credit_card_form.visibility = View.GONE
    payment_methods.visibility = View.INVISIBLE
    loading.visibility = View.VISIBLE
  }

  override fun showPaymentDetailsForm() {
    payment_methods.visibility = View.GONE
    loading.visibility = View.GONE
    fragment_braintree_credit_card_form.visibility = View.VISIBLE
  }

  override fun showPaymentMethods() {
    fragment_braintree_credit_card_form.visibility = View.GONE
    loading.visibility = View.GONE
    payment_methods.visibility = View.VISIBLE
  }

  override fun rotateChangeCurrencyButton() {
    val rotateAnimation = RotateAnimation(
        0f,
        180f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f)
    rotateAnimation.duration = 250
    rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
    swap_value_button.startAnimation(rotateAnimation)
  }
}
