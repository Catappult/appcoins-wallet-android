package com.asfoundation.wallet.wallet_validation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_phone_validation.*
import javax.inject.Inject

class PhoneValidationFragment : DaggerFragment(), PhoneValidationView {

  @Inject
  lateinit var interactor: SmsValidationInteract

  private var walletValidationView: WalletValidationView? = null
  private lateinit var presenter: PhoneValidationPresenter

  private var countryCode: String? = null
  private var phoneNumber: String? = null
  private var errorMessage: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    presenter =
        PhoneValidationPresenter(this, walletValidationView, interactor,
            AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable())
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

  override fun setupUI() {
    val arr = PhoneNumberUtil.getInstance()
        .supportedCallingCodes.toTypedArray()

    val adapter = ArrayAdapter(context!!, R.layout.spinner_item, arr)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
    country_code?.adapter = adapter

    countryCode?.let {
      val position = arr.indexOf(it.toInt())
      country_code.setSelection(position)
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

  override fun getCountryCode(): Observable<String> {
    return RxAdapterView.itemSelections(country_code)
        .map {
          val arr = PhoneNumberUtil.getInstance()
              .supportedRegions.toTypedArray()
          arr[it]
        }
  }

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
        .map { Pair(country_code.selectedItem.toString(), phone_number.text.toString()) }
  }

  override fun getCancelClicks(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is WalletValidationView) {
      throw IllegalStateException(
          "PhoneValidationFragment must be attached to Wallet Validation activity")
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
      bundle.putString(COUNTRY_CODE, countryCode)
      bundle.putString(PHONE_NUMBER, phoneNumber)

      errorMessage?.let { bundle.putInt(ERROR_MESSAGE, errorMessage) }

      val fragment = PhoneValidationFragment()
      fragment.arguments = bundle
      return fragment
    }

  }

}
