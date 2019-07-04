package com.asfoundation.wallet.wallet_validation

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.util.isNotNumeric
import com.asfoundation.wallet.widget.PasteEditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sms_code.*
import kotlinx.android.synthetic.main.single_sms_input_layout.view.*
import kotlinx.android.synthetic.main.sms_text_input_layout.*
import javax.inject.Inject


class CodeValidationFragment : DaggerFragment(), CodeValidationView,
    PasteEditText.OnPasteListener {

  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  private var walletValidationView: WalletValidationView? = null
  private lateinit var presenter: CodeValidationPresenter
  private lateinit var fragmentContainer: ViewGroup

  val countryCode: String by lazy {
    if (arguments!!.containsKey(PhoneValidationFragment.COUNTRY_CODE)) {
      arguments!!.getString(PhoneValidationFragment.COUNTRY_CODE)
    } else {
      throw IllegalArgumentException("Country Code not passed")
    }
  }

  val phoneNumber: String by lazy {
    if (arguments!!.containsKey(PhoneValidationFragment.PHONE_NUMBER)) {
      arguments!!.getString(PhoneValidationFragment.PHONE_NUMBER)
    } else {
      throw IllegalArgumentException("Phone Number not passed")
    }
  }

  private val errorMessage: Int? by lazy {
    if (arguments!!.containsKey(ERROR_MESSAGE)) {
      arguments!!.getInt(ERROR_MESSAGE)
    } else {
      null
    }
  }

  private val validationInfo: ValidationInfo? by lazy {
    if (arguments!!.containsKey(VALIDATION_INFO)) {
      arguments!!.getSerializable(VALIDATION_INFO) as ValidationInfo
    } else {
      null
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    presenter =
        CodeValidationPresenter(this, walletValidationView,
            smsValidationInteract, AndroidSchedulers.mainThread(), Schedulers.io(), countryCode,
            phoneNumber, CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_sms_code, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onPaste() {
    val text = getValidTextFromClipboard()

    text?.forEachIndexed { i, c ->
      when (i) {
        0 -> code_1.code.setText(c.toString())
        1 -> code_2.code.setText(c.toString())
        2 -> code_3.code.setText(c.toString())
        3 -> code_4.code.setText(c.toString())
        4 -> code_5.code.setText(c.toString())
        5 -> code_6.code.setText(c.toString())
        else -> return@forEachIndexed
      }
    }
  }

  private fun getValidTextFromClipboard(): String? {
    var clipboard = context!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    if (clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == false) {
      return null
    }

    val text = clipboard.primaryClip?.getItemAt(0)
        ?.text

    if (text != null && text.toString().isNotNumeric()) {
      return null
    }

    return text.toString()
  }

  override fun setupUI() {
    if (errorMessage == null) {
      error.visibility = View.INVISIBLE
      setButtonState(true)
    } else {
      error.visibility = View.VISIBLE
      error.text = getString(errorMessage!!)
      setButtonState(false)
    }

    validationInfo?.let {
      code_1.code.setText(it.code1)
      code_2.code.setText(it.code2)
      code_3.code.setText(it.code3)
      code_4.code.setText(it.code4)
      code_5.code.setText(it.code5)
      code_6.code.setText(it.code6)
    }

    code_1.code.setOnPasteListener(this)
    code_2.code.setOnPasteListener(this)
    code_3.code.setOnPasteListener(this)
    code_4.code.setOnPasteListener(this)
    code_5.code.setOnPasteListener(this)
    code_6.code.setOnPasteListener(this)
  }

  override fun clearUI() {
    error.visibility = View.INVISIBLE
    code_1.code.text = null
    code_2.code.text = null
    code_3.code.text = null
    code_4.code.text = null
    code_5.code.text = null
    code_6.code.text = null
  }

  override fun setButtonState(state: Boolean) {
    submit_button.isEnabled = state
  }

  override fun getBackClicks(): Observable<Any> {
    return RxView.clicks(back_button)
  }

  override fun getSubmitClicks(): Observable<ValidationInfo> {
    return RxView.clicks(submit_button)
        .map {
          ValidationInfo(code_1.code.text.toString(),
              code_2.code.text.toString(), code_3.code.text.toString(),
              code_4.code.text.toString(), code_5.code.text.toString(),
              code_6.code.text.toString(), countryCode,
              phoneNumber)
        }
  }

  override fun getResentCodeClicks(): Observable<Any> {
    return RxView.clicks(resend_code)
  }

  override fun getFirstChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_1.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getSecondChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_2.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getThirdChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_3.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getFourthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_4.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getFifthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_5.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getSixthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_6.code)
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun moveToNextView(current: Int) {
    when (current) {
      1 -> code_2.requestFocus()
      2 -> code_3.requestFocus()
      3 -> code_4.requestFocus()
      4 -> code_5.requestFocus()
      5 -> code_6.requestFocus()
    }
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is WalletValidationView) {
      throw IllegalStateException(
          "CodeValidationFragment must be attached to Wallet Validation activity")
    }

    walletValidationView = context
  }

  override fun onDetach() {
    super.onDetach()
    walletValidationView = null
  }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
    title.requestFocus()
  }

  companion object {

    internal const val ERROR_MESSAGE = "ERROR_MESSAGE"
    internal const val VALIDATION_INFO = "VALIDATION_INFO"

    @JvmStatic
    fun newInstance(countryCode: String, phoneNumber: String): Fragment {
      val bundle = Bundle()
      bundle.putString(PhoneValidationFragment.COUNTRY_CODE, countryCode)
      bundle.putString(PhoneValidationFragment.PHONE_NUMBER, phoneNumber)

      val fragment = CodeValidationFragment()
      fragment.arguments = bundle
      return fragment
    }

    @JvmStatic
    fun newInstance(info: ValidationInfo, errorMessage: Int): Fragment {
      val bundle = Bundle()
      bundle.putString(PhoneValidationFragment.COUNTRY_CODE, info.countryCode)
      bundle.putString(PhoneValidationFragment.PHONE_NUMBER, info.phoneNumber)
      bundle.putInt(ERROR_MESSAGE, errorMessage)
      bundle.putSerializable(VALIDATION_INFO, info)

      val fragment = CodeValidationFragment()
      fragment.arguments = bundle
      return fragment
    }

  }

}
