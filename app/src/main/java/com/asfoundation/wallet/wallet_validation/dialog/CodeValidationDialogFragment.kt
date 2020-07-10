package com.asfoundation.wallet.wallet_validation.dialog

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.DeleteKeyListener
import com.asfoundation.wallet.wallet_validation.PasteTextWatcher
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
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


class CodeValidationDialogFragment : DaggerFragment(),
    CodeValidationDialogView {

  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  @Inject
  lateinit var analytics: WalletValidationAnalytics

  private var walletValidationDialogView: WalletValidationDialogView? = null
  private lateinit var dialogPresenter: CodeValidationDialogPresenter
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var clipboard: ClipboardManager

  val countryCode: String by lazy {
    if (arguments!!.containsKey(PhoneValidationDialogFragment.COUNTRY_CODE)) {
      arguments!!.getString(PhoneValidationDialogFragment.COUNTRY_CODE)
    } else {
      throw IllegalArgumentException("Country Code not passed")
    }
  }

  val phoneNumber: String by lazy {
    if (arguments!!.containsKey(PhoneValidationDialogFragment.PHONE_NUMBER)) {
      arguments!!.getString(PhoneValidationDialogFragment.PHONE_NUMBER)
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

    clipboard = context!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    dialogPresenter =
        CodeValidationDialogPresenter(this, walletValidationDialogView, smsValidationInteract,
            AndroidSchedulers.mainThread(), Schedulers.io(), countryCode, phoneNumber,
            CompositeDisposable(), analytics)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_sms_code, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dialogPresenter.present()
  }

  override fun onResume() {
    super.onResume()

    focusAndShowKeyboard(code_1.code)
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

    val inputTexts =
        arrayOf(code_1.code, code_2.code, code_3.code, code_4.code, code_5.code, code_6.code)

    code_1.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 0))
    code_2.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 1))
    code_3.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 2))
    code_4.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 3))
    code_5.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 4))
    code_6.code.addTextChangedListener(PasteTextWatcher(inputTexts, clipboard, 5))
    code_1.code.setOnKeyListener(DeleteKeyListener(inputTexts, 0))
    code_2.code.setOnKeyListener(DeleteKeyListener(inputTexts, 1))
    code_3.code.setOnKeyListener(DeleteKeyListener(inputTexts, 2))
    code_4.code.setOnKeyListener(DeleteKeyListener(inputTexts, 3))
    code_5.code.setOnKeyListener(DeleteKeyListener(inputTexts, 4))
    code_6.code.setOnKeyListener(DeleteKeyListener(inputTexts, 5))
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

  override fun getBackClicks() = RxView.clicks(back_button)

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

  override fun getResentCodeClicks() = RxView.clicks(resend_code)


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
    dialogPresenter.stop()
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is WalletValidationDialogView) {
      throw IllegalStateException(
          "CodeValidationFragment must be attached to Wallet Validation activity")
    }

    walletValidationDialogView = context
  }

  override fun onDetach() {
    super.onDetach()
    walletValidationDialogView = null
  }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
    code_6.clearFocus()
  }

  companion object {

    internal const val ERROR_MESSAGE = "ERROR_MESSAGE"
    internal const val VALIDATION_INFO = "VALIDATION_INFO"

    @JvmStatic
    fun newInstance(countryCode: String, phoneNumber: String): Fragment {
      val bundle = Bundle().apply {
        putString(PhoneValidationDialogFragment.COUNTRY_CODE, countryCode)
        putString(PhoneValidationDialogFragment.PHONE_NUMBER, phoneNumber)
      }

      return CodeValidationDialogFragment().apply { arguments = bundle }
    }

    @JvmStatic
    fun newInstance(info: ValidationInfo, errorMessage: Int): Fragment {
      val bundle = Bundle().apply {
        putString(PhoneValidationDialogFragment.COUNTRY_CODE, info.countryCode)
        putString(PhoneValidationDialogFragment.PHONE_NUMBER, info.phoneNumber)
        putInt(ERROR_MESSAGE, errorMessage)
        putSerializable(VALIDATION_INFO, info)
      }

      return CodeValidationDialogFragment().apply { arguments = bundle }
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
