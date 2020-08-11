package com.asfoundation.wallet.wallet_validation.dialog

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
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import com.hbb20.CountryCodePicker
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_phone_validation.*
import javax.inject.Inject


class PhoneValidationDialogFragment : DaggerFragment(),
    PhoneValidationDialogView {

  @Inject
  lateinit var interactor: SmsValidationInteract

  @Inject
  lateinit var analytics: WalletValidationAnalytics

  private var walletValidationDialogView: WalletValidationDialogView? = null
  private lateinit var presenter: PhoneValidationDialogPresenter

  private var countryCode: String? = null
  private var phoneNumber: String? = null
  private var errorMessage: Int? = null

  companion object {

    internal const val COUNTRY_CODE = "COUNTRY_CODE"
    internal const val PHONE_NUMBER = "PHONE_NUMBER"
    internal const val ERROR_MESSAGE = "ERROR_MESSAGE"

    @JvmStatic
    fun newInstance(countryCode: String? = null, phoneNumber: String? = null,
                    errorMessage: Int? = null): Fragment {
      val bundle = Bundle().apply {
        putString(COUNTRY_CODE, countryCode)
        putString(PHONE_NUMBER, phoneNumber)
      }

      errorMessage?.let {
        bundle.putInt(ERROR_MESSAGE, errorMessage)
      }

      return PhoneValidationDialogFragment().apply { arguments = bundle }
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    presenter =
        PhoneValidationDialogPresenter(this,
            walletValidationDialogView, interactor,
            AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), analytics)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_phone_validation, container, false)
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

    presenter.present()
  }

  override fun onResume() {
    super.onResume()

    presenter.onResume()
    focusAndShowKeyboard(phone_number)
  }

  override fun setupUI() {
    country_code_picker.registerCarrierNumberEditText(phone_number)
    country_code_picker.setCustomDialogTextProvider(object :
        CountryCodePicker.CustomDialogTextProvider {
      override fun getCCPDialogSearchHintText(language: CountryCodePicker.Language?,
                                              defaultSearchHintText: String?) =
          defaultSearchHintText ?: ""

      override fun getCCPDialogTitle(language: CountryCodePicker.Language?, defaultTitle: String?) =
          getString(R.string.verification_insert_phone_field_country)

      override fun getCCPDialogNoResultACK(language: CountryCodePicker.Language?,
                                           defaultNoResultACK: String?) = defaultNoResultACK ?: ""
    })
    
    countryCode?.let {
      country_code_picker.setCountryForPhoneCode(it.drop(0)
          .toInt())
    }
    phoneNumber?.let { phone_number.setText(it) }

    errorMessage?.let { setError(it) }

  }

  override fun setError(message: Int) {
    phone_number_layout.error = getString(message)
  }

  override fun clearError() {
    phone_number_layout.error = null
  }

  override fun getCountryCode() = Observable.just(country_code_picker.selectedCountryCodeWithPlus)


  override fun getPhoneNumber(): Observable<String> {
    return RxTextView.afterTextChangeEvents(phone_number)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun setButtonState(state: Boolean) {
    submit_button.isEnabled = state
  }

  override fun getSubmitClicks(): Observable<Pair<String, String>> {
    return RxView.clicks(submit_button)
        .map {
          Pair(country_code_picker.selectedCountryCodeWithPlus,
              country_code_picker.fullNumber.substringAfter(
                  country_code_picker.selectedCountryCode))
        }
  }

  override fun getCancelClicks() = RxView.clicks(cancel_button)

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is WalletValidationDialogView) {
      throw IllegalStateException(
          "PoaPhoneValidationFragment must be attached to Wallet Validation activity")
    }

    walletValidationDialogView = context
  }

  override fun onDetach() {
    super.onDetach()
    walletValidationDialogView = null
  }

  private fun focusAndShowKeyboard(view: EditText) {
    view.post {
      view.requestFocus()
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
  }

}
