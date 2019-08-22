package com.asfoundation.wallet.wallet_validation.generic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_phone_validation.*
import kotlinx.android.synthetic.main.layout_validation_no_internet.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PhoneValidationFragment : DaggerFragment(),
    PhoneValidationView {

  @Inject
  lateinit var interactor: SmsValidationInteract

  private var walletValidationView: WalletValidationView? = null
  private lateinit var presenter: PhoneValidationPresenter
  private lateinit var fragmentContainer: ViewGroup

  private var countryCode: String? = null
  private var phoneNumber: String? = null
  private var errorMessage: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    presenter =
        PhoneValidationPresenter(this,
            walletValidationView, interactor,
            AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.layout_phone_validation, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (arguments?.containsKey(
            COUNTRY_CODE) == true) {
      countryCode = arguments?.getString(
          COUNTRY_CODE)
    }
    if (arguments?.containsKey(
            PHONE_NUMBER) == true) {
      phoneNumber = arguments?.getString(
          PHONE_NUMBER)
    }
    if (arguments?.containsKey(
            ERROR_MESSAGE) == true) {
      errorMessage = arguments?.getInt(
          ERROR_MESSAGE)
    }

    presenter.present()
  }

  override fun onResume() {
    super.onResume()

    focusAndShowKeyboard(phone_number)
  }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
    content_main.requestFocus()
  }

  override fun showNoInternetView() {
    walletValidationView?.hideProgressAnimation()
    stopRetryAnimation()
    content_main!!.visibility = View.GONE
    layout_validation_no_internet!!.visibility = View.VISIBLE
  }

  override fun hideNoInternetView() {
    walletValidationView?.showProgressAnimation()
    content_main!!.visibility = View.VISIBLE
    layout_validation_no_internet!!.visibility = View.GONE
  }

  override fun getRetryButtonClicks(): Observable<Pair<String, String>> {
    return RxView.clicks(retry_button)
        .map {
          Pair(ccp.selectedCountryCodeWithPlus,
              ccp.fullNumber.substringAfter(ccp.selectedCountryCode))
        }
        .doOnNext { playRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
  }

  override fun getLaterButtonClicks(): Observable<Any> {
    return RxView.clicks(later_button)
  }

  override fun setupUI() {
    ccp.registerCarrierNumberEditText(phone_number)

    hideNoInternetView()

    countryCode?.let {
      ccp.setCountryForPhoneCode(it.drop(0).toInt())
    }
    phoneNumber?.let { phone_number.setText(it) }

    errorMessage?.let { setError(it) }
  }

  override fun setError(message: Int) {
    phone_number_layout.error = getString(message)
    hideNoInternetView()
  }

  override fun clearError() {
    phone_number_layout.error = null
  }

  override fun getCountryCode(): Observable<String> {
    return Observable.just(ccp.selectedCountryCodeWithPlus)
  }

  override fun getPhoneNumber(): Observable<String> {
    return RxTextView.afterTextChangeEvents(phone_number)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun setButtonState(state: Boolean) {
    next_button.isEnabled = state
  }

  override fun getNextClicks(): Observable<Pair<String, String>> {
    return RxView.clicks(next_button)
        .map {
          Pair(ccp.selectedCountryCodeWithPlus,
              ccp.fullNumber.substringAfter(ccp.selectedCountryCode))
        }
  }

  override fun getCancelClicks(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  private fun stopRetryAnimation() {
    retry_button!!.visibility = View.VISIBLE
    later_button!!.visibility = View.VISIBLE
    retry_animation!!.visibility = View.GONE
  }

  private fun playRetryAnimation() {
    retry_button!!.visibility = View.GONE
    later_button!!.visibility = View.GONE
    retry_animation!!.visibility = View.VISIBLE
    retry_animation!!.playAnimation()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is WalletValidationView) {
      throw IllegalStateException(
          "PoaPhoneValidationFragment must be attached to Wallet Validation activity")
    }

    walletValidationView = context
  }

  override fun onDetach() {
    super.onDetach()
    walletValidationView = null
  }

  companion object {

    internal const val COUNTRY_CODE = "COUNTRY_CODE"
    internal const val PHONE_NUMBER = "PHONE_NUMBER"
    internal const val ERROR_MESSAGE = "ERROR_MESSAGE"

    @JvmStatic
    fun newInstance(countryCode: String? = null, phoneNumber: String? = null,
                    errorMessage: Int? = null): Fragment {
      val bundle = Bundle()
      bundle.putString(
          COUNTRY_CODE, countryCode)
      bundle.putString(
          PHONE_NUMBER, phoneNumber)

      errorMessage?.let {
        bundle.putInt(
            ERROR_MESSAGE, errorMessage)
      }

      val fragment = PhoneValidationFragment()
      fragment.arguments = bundle
      return fragment
    }

  }

  private fun focusAndShowKeyboard(view: EditText) {
    view.post {
      view.requestFocus()
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
  }

}
