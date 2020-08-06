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
import com.asfoundation.wallet.logging.Logger
import com.hbb20.CountryCodePicker
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

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var analytics: WalletValidationAnalytics
  private var walletValidationView: WalletValidationView? = null
  private lateinit var presenter: PhoneValidationPresenter
  private lateinit var fragmentContainer: ViewGroup

  private var countryCode: String? = null
  private var phoneNumber: String? = null
  private var errorMessage: Int? = null
  private var previousContext: String = ""

  private val hasBeenInvitedFlow: Boolean by lazy {
    arguments!!.getBoolean(HAS_BEEN_INVITED_FLOW)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    presenter = PhoneValidationPresenter(this, walletValidationView, interactor, logger,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), analytics)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.layout_phone_validation, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (arguments?.containsKey(COUNTRY_CODE) == true) {
      countryCode = arguments?.getString(COUNTRY_CODE)
    }
    if (arguments?.containsKey(PHONE_NUMBER) == true) {
      phoneNumber = arguments?.getString(PHONE_NUMBER)
    }
    if (arguments?.containsKey(ERROR_MESSAGE) == true) {
      errorMessage = arguments?.getInt(ERROR_MESSAGE)
    }
    if (arguments?.containsKey(PREVIOUS_CONTEXT) == true) {
      previousContext = arguments?.getString(PREVIOUS_CONTEXT, "") ?: ""
    }

    handleOnSavedInstance(savedInstanceState)

    setupBodyText()
    presenter.present()
  }

  private fun handleOnSavedInstance(savedInstanceState: Bundle?) {
    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(COUNTRY_CODE)) {
        countryCode = savedInstanceState.getString(COUNTRY_CODE)
      }
      if (savedInstanceState.containsKey(ERROR_MESSAGE)) {
        errorMessage = savedInstanceState.getInt(ERROR_MESSAGE)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putString(COUNTRY_CODE, ccp.selectedCountryCode)
    errorMessage?.let { outState.putInt(ERROR_MESSAGE, it) }
  }

  override fun onResume() {
    super.onResume()

    presenter.onResume(errorMessage)
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
    content_main.visibility = View.GONE
    layout_validation_no_internet.visibility = View.VISIBLE
    if (!hasBeenInvitedFlow) later_button.visibility = View.GONE
  }

  override fun hideNoInternetView() {
    walletValidationView?.showProgressAnimation()
    content_main.visibility = View.VISIBLE
    layout_validation_no_internet.visibility = View.GONE
  }

  override fun getRetryButtonClicks(): Observable<PhoneValidationClickData> {
    return RxView.clicks(retry_button)
        .map {
          PhoneValidationClickData(ccp.selectedCountryCodeWithPlus,
              ccp.fullNumber.substringAfter(ccp.selectedCountryCode), previousContext)
        }
        .doOnNext { playRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
  }

  override fun getLaterButtonClicks(): Observable<PhoneValidationClickData> {
    return RxView.clicks(later_button)
        .map { PhoneValidationClickData("", "", previousContext) }
  }

  override fun setupUI() {
    ccp.registerCarrierNumberEditText(phone_number)
    ccp.setCustomDialogTextProvider(object : CountryCodePicker.CustomDialogTextProvider {
      override fun getCCPDialogSearchHintText(language: CountryCodePicker.Language?,
                                              defaultSearchHintText: String?): String {
        return defaultSearchHintText ?: ""
      }

      override fun getCCPDialogTitle(language: CountryCodePicker.Language?,
                                     defaultTitle: String?): String {
        return getString(R.string.verification_insert_phone_field_country)
      }

      override fun getCCPDialogNoResultACK(language: CountryCodePicker.Language?,
                                           defaultNoResultACK: String?): String {
        return defaultNoResultACK ?: ""
      }
    })

    hideNoInternetView()

    countryCode?.let {
      ccp.setCountryForPhoneCode(it.drop(0)
          .toInt())
    }
    phoneNumber?.let { phone_number.setText(it) }

    errorMessage?.let { setError(it) }
  }

  override fun setError(message: Int) {
    phone_number_layout.error = getString(message)
    errorMessage = message
    hideNoInternetView()
  }

  override fun clearError() {
    phone_number_layout.error = null
    // This check is needed because this method is always called when restoring the view state and we only want to clear the error when it is the user triggering the changes.
    if (isResumed) {
      errorMessage = null
    }
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

  override fun getNextClicks(): Observable<PhoneValidationClickData> {
    return RxView.clicks(next_button)
        .map {
          PhoneValidationClickData(ccp.selectedCountryCodeWithPlus,
              ccp.fullNumber.substringAfter(ccp.selectedCountryCode), previousContext)
        }
  }

  override fun getCancelClicks(): Observable<PhoneValidationClickData> {
    return RxView.clicks(cancel_button)
        .map { PhoneValidationClickData("", "", previousContext) }
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  private fun stopRetryAnimation() {
    retry_button.visibility = View.VISIBLE
    if (!hasBeenInvitedFlow) later_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
  }

  private fun playRetryAnimation() {
    retry_button.visibility = View.GONE
    later_button.visibility = View.GONE
    retry_animation.visibility = View.VISIBLE
    retry_animation.playAnimation()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(
        context is WalletValidationView) { PhoneValidationFragment::class.java.simpleName + " needs to be attached to a " + WalletValidationView::class.java.simpleName }

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
    internal const val HAS_BEEN_INVITED_FLOW = "HAS_BEEN_INVITED_FLOW"

    private const val PREVIOUS_CONTEXT = "PREVIOUS_CONTEXT"

    @JvmStatic
    fun newInstance(countryCode: String? = null, phoneNumber: String? = null,
                    errorMessage: Int? = null, hasBeenInvitedFlow: Boolean = true,
                    previousContext: String? = ""): Fragment {
      val bundle = Bundle().apply {
        putString(COUNTRY_CODE, countryCode)
        putString(PHONE_NUMBER, phoneNumber)
        putBoolean(HAS_BEEN_INVITED_FLOW, hasBeenInvitedFlow)
        putString(PREVIOUS_CONTEXT, previousContext)

        errorMessage?.let { putInt(ERROR_MESSAGE, errorMessage) }
      }

      return PhoneValidationFragment().apply { arguments = bundle }
    }

    data class PhoneValidationClickData(val countryCode: String, val number: String,
                                        val previousContext: String)
  }

  private fun focusAndShowKeyboard(view: EditText) {
    view.post {
      view.requestFocus()
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
  }

  private fun setupBodyText() {
    if (!hasBeenInvitedFlow) {
      phone_validation_subtitle.text = getString(R.string.verification_insert_phone_body)
    }
  }

}
