package com.asfoundation.wallet.wallet_validation.generic

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
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.wallet_validation.DeleteKeyListener
import com.asfoundation.wallet.wallet_validation.PasteTextWatcher
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_code_validation.*
import kotlinx.android.synthetic.main.layout_referral_status.*
import kotlinx.android.synthetic.main.layout_validation_no_internet.*
import kotlinx.android.synthetic.main.layout_validation_result.*
import kotlinx.android.synthetic.main.single_sms_input_layout.view.*
import kotlinx.android.synthetic.main.sms_text_input_layout.*
import javax.inject.Inject


class CodeValidationFragment : DaggerFragment(),
    CodeValidationView {

  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  @Inject
  lateinit var defaultWalletInteract: FindDefaultWalletInteract

  @Inject
  lateinit var analytics: WalletValidationAnalytics

  private var walletValidationView: WalletValidationView? = null
  private lateinit var presenter: CodeValidationPresenter
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var clipboard: ClipboardManager
  private var code: Code? = null

  private val hasBeenInvitedFlow: Boolean by lazy {
    arguments!!.getBoolean(HAS_BEEN_INVITED_FLOW)
  }

  val countryCode: String by lazy {
    try {
      arguments!!.getString(PhoneValidationFragment.COUNTRY_CODE)!!
    } catch (e: Exception) {
      throw IllegalArgumentException("Unable to get Country Code")
    }
  }

  val phoneNumber: String by lazy {
    try {
      arguments!!.getString(PhoneValidationFragment.PHONE_NUMBER)!!
    } catch (e: Exception) {
      throw IllegalArgumentException("Unable to get Phone Number")
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

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putString(CODE_1, code_1.code.text.toString())
    outState.putString(CODE_2, code_2.code.text.toString())
    outState.putString(CODE_3, code_3.code.text.toString())
    outState.putString(CODE_4, code_4.code.text.toString())
    outState.putString(CODE_5, code_5.code.text.toString())
    outState.putString(CODE_6, code_6.code.text.toString())
    outState.putBoolean(IS_LAST_STEP, presenter.isLastStep)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    clipboard = context!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    presenter =
        CodeValidationPresenter(this, walletValidationView, referralInteractor,
            smsValidationInteract, defaultWalletInteract, AndroidSchedulers.mainThread(),
            Schedulers.io(), countryCode, phoneNumber, CompositeDisposable(), hasBeenInvitedFlow,
            analytics)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.layout_code_validation, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupBodyText()

    var lastStep = false
    savedInstanceState?.let {
      lastStep = it.getBoolean(IS_LAST_STEP)
      code = Code(it.getString(CODE_1), it.getString(CODE_2), it.getString(CODE_3),
          it.getString(CODE_4), it.getString(CODE_5), it.getString(CODE_6))
    }

    presenter.present(lastStep)
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume(code)
  }

  override fun setupUI() {
    hideNoInternetView()
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
    code_1.requestFocus()
  }

  override fun setButtonState(state: Boolean) {
    submit_button.isEnabled = state
  }

  override fun getBackClicks() = RxView.clicks(back_button)

  override fun getRetryButtonClicks(): Observable<ValidationInfo> {
    return RxView.clicks(retry_button)
        .map {
          ValidationInfo(code_1.code.text.toString(),
              code_2.code.text.toString(), code_3.code.text.toString(),
              code_4.code.text.toString(), code_5.code.text.toString(),
              code_6.code.text.toString(), countryCode,
              phoneNumber)
        }
  }

  override fun getLaterButtonClicks() = RxView.clicks(later_button)

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

  override fun getOkClicks() = RxView.clicks(ok_button)

  override fun getResentCodeClicks() = RxView.clicks(resend_code)

  override fun getFirstChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_1.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              .toString()
        }
  }

  override fun getSecondChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_2.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              .toString()
        }
  }

  override fun getThirdChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_3.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              .toString()
        }
  }

  override fun getFourthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_4.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              .toString()
        }
  }

  override fun getFifthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_5.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              ?.toString()
        }
  }

  override fun getSixthChar(): Observable<String> {
    return RxTextView.afterTextChangeEvents(code_6.code)
        .filter { it.editable() != null }
        .map {
          it.editable()
              .toString()
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

  override fun showLoading() {
    content.visibility = View.GONE
    referral_status.visibility = View.GONE
    animation_validating_code.visibility = View.VISIBLE
    validate_code_animation.playAnimation()
  }

  override fun showReferralEligible(currency: String, maxAmount: String, minAmount: String) {
    walletValidationView?.showLastStepAnimation()
    content.visibility = View.GONE
    animation_validating_code.visibility = View.GONE
    referral_status.visibility = View.VISIBLE
    referral_status_title.setText(R.string.referral_verification_confirmation_title)
    referral_status_body.text =
        getString(R.string.referral_verification_confirmation_body,
            currency + maxAmount, currency + minAmount)
    referral_status_animation.setAnimation(R.raw.referral_invited)
    referral_status_animation.playAnimation()
  }

  override fun showReferralIneligible(currency: String, maxAmount: String) {
    walletValidationView?.showLastStepAnimation()
    content.visibility = View.GONE
    animation_validating_code.visibility = View.GONE
    referral_status.visibility = View.VISIBLE
    referral_status_title.setText(R.string.referral_verification_not_invited_title)
    referral_status_body.text =
        getString(R.string.referral_verification_not_invited_body, currency + maxAmount)
    referral_status_animation.setAnimation(R.raw.referral_not_invited)
    referral_status_animation.playAnimation()
  }

  override fun showGenericValidationComplete() {
    walletValidationView?.showLastStepAnimation()
    content.visibility = View.GONE
    animation_validating_code.visibility = View.GONE
    referral_status.visibility = View.VISIBLE
    referral_status_title.setText(R.string.verification_completed_title)
    referral_status_body.setText(R.string.verification_completed_body)
    referral_status_animation.setAnimation(R.raw.referral_invited)
    referral_status_animation.playAnimation()
  }

  override fun setCode(code: Code) {
    code.code1?.let { code_1.code.setText(it) }
    code.code2?.let { code_2.code.setText(it) }
    code.code3?.let { code_3.code.setText(it) }
    code.code4?.let { code_4.code.setText(it) }
    code.code5?.let { code_5.code.setText(it) }
    code.code6?.let { code_6.code.setText(it) }
    this.code = null
  }

  override fun showNoInternetView() {
    walletValidationView?.hideProgressAnimation()
    stopRetryAnimation()
    content.visibility = View.GONE
    referral_status.visibility = View.GONE
    animation_validating_code.visibility = View.GONE
    layout_validation_no_internet.visibility = View.VISIBLE
  }

  override fun hideNoInternetView() {
    walletValidationView?.showProgressAnimation()
    layout_validation_no_internet.visibility = View.GONE
  }

  private fun stopRetryAnimation() {
    retry_button.visibility = View.VISIBLE
    later_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(
        context is WalletValidationView) { CodeValidationFragment::class.java.simpleName + " needs to be attached to a " + WalletValidationView::class.java.simpleName }

    walletValidationView = context
  }

  override fun onDetach() {
    super.onDetach()
    walletValidationView = null
  }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(fragmentContainer.windowToken, 0)
    code_6.clearFocus()
  }

  companion object {

    internal const val ERROR_MESSAGE = "ERROR_MESSAGE"
    internal const val VALIDATION_INFO = "VALIDATION_INFO"
    internal const val HAS_BEEN_INVITED_FLOW = "HAS_BEEN_INVITED_FLOW"
    internal const val CODE_1 = "code1"
    internal const val CODE_2 = "code2"
    internal const val CODE_3 = "code3"
    internal const val CODE_4 = "code4"
    internal const val CODE_5 = "code5"
    internal const val CODE_6 = "code6"
    internal const val IS_LAST_STEP = "lastStep"

    @JvmStatic
    fun newInstance(countryCode: String, phoneNumber: String,
                    hasBeenInvitedFlow: Boolean = true): Fragment {
      val bundle = Bundle().apply {
        putString(PhoneValidationFragment.COUNTRY_CODE, countryCode)
        putString(PhoneValidationFragment.PHONE_NUMBER, phoneNumber)
        putBoolean(HAS_BEEN_INVITED_FLOW, hasBeenInvitedFlow)
      }

      return CodeValidationFragment().apply { arguments = bundle }
    }

    @JvmStatic
    fun newInstance(info: ValidationInfo, errorMessage: Int,
                    hasBeenInvitedFlow: Boolean = true): Fragment {
      val bundle = Bundle().apply {
        putString(PhoneValidationFragment.COUNTRY_CODE, info.countryCode)
        putString(PhoneValidationFragment.PHONE_NUMBER, info.phoneNumber)
        putInt(ERROR_MESSAGE, errorMessage)
        putSerializable(VALIDATION_INFO, info)
        putBoolean(HAS_BEEN_INVITED_FLOW, hasBeenInvitedFlow)
      }

      return CodeValidationFragment().apply { arguments = bundle }
    }
  }

  override fun focusAndShowKeyboard() {
    val view = getViewToFocus()
    view?.post {
      view.requestFocus()
      val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      imm?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
  }

  private fun getViewToFocus(): View? {
    return when {
      code_1.code.text.isBlank() -> code_1.code
      code_2.code.text.isBlank() -> code_2.code
      code_3.code.text.isBlank() -> code_3.code
      code_4.code.text.isBlank() -> code_4.code
      code_5.code.text.isBlank() -> code_5.code
      code_6.code.text.isBlank() -> code_6.code
      else -> null
    }
  }

  private fun setupBodyText() {
    if (!hasBeenInvitedFlow) {
      code_validation_subtitle.text = getString(R.string.verification_insert_phone_body)
    }
  }
}
