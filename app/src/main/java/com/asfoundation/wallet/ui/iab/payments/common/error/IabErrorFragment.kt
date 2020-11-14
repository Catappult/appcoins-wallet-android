package com.asfoundation.wallet.ui.iab.payments.common.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_buy_buttons.*
import kotlinx.android.synthetic.main.fragment_iab_error.*
import javax.inject.Inject

class IabErrorFragment : DaggerFragment(), IabErrorView {

  @Inject
  lateinit var presenter: IabErrorPresenter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_iab_error, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun setupUi() {
    buy_button.text = getString(R.string.back_button)
    cancel_button.text = getString(R.string.button_cancel)
  }

  override fun cancelClickEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun backClickEvent(): Observable<Any> {
    return RxView.clicks(buy_button)
  }

  override fun otherPaymentMethodsClickEvent(): Observable<Any> {
    return RxView.clicks(more_payment_methods)
  }

  override fun setErrorMessage(errorMessage: String) {
    error_message.text = errorMessage
  }

  override fun setSupportVisibility(showSupport: Boolean) {
    if (showSupport) {
      layout_support_logo.visibility = View.VISIBLE
      layout_support_icn.visibility = View.VISIBLE
      contact_us.visibility = View.VISIBLE
      more_payment_methods.visibility = View.GONE
    } else {
      layout_support_logo.visibility = View.GONE
      layout_support_icn.visibility = View.GONE
      contact_us.visibility = View.GONE
      more_payment_methods.visibility = View.VISIBLE
    }

  }

  companion object {

    internal const val ERROR_MESSAGE_STRING = "error_message_string"
    internal const val ERROR_MESSAGE_RESOURCE = "error_message_resource"
    internal const val FEATURE_ENTRY_BACKSTACK_NAME = "backstack_name"
    internal const val SHOW_SUPPORT = "show_support"

    /**
     * Creates a new instance of IabErrorFragment. Note that the entry fragment of the payment
     * should be added to the backstack with a name and passed here so that it manages navigation
     * correctly.
     *
     * The entry fragment of a payment is the first fragment of a payment when the user selects
     * the payment in PaymentsMethod and navigates to it.
     *
     * @param errorMessage Message description in the error fragment
     * @param entryBackStackName The backstack name given when adding the payment entry fragment
     *                           to the backstack
     * @param showSupport If the error should show support redirect
     */
    @JvmStatic
    fun newInstance(errorMessage: String, entryBackStackName: String,
                    showSupport: Boolean): IabErrorFragment {
      val fragment =
          IabErrorFragment()

      fragment.arguments = Bundle().apply {
        putString(
            ERROR_MESSAGE_STRING, errorMessage)
        putString(
            FEATURE_ENTRY_BACKSTACK_NAME, entryBackStackName)
        putBoolean(
            SHOW_SUPPORT, showSupport)
      }
      return fragment
    }

    @JvmStatic
    fun newInstance(@StringRes errorMessageResource: Int,
                    entryBackStackName: String,
                    showSupport: Boolean): IabErrorFragment {
      val fragment =
          IabErrorFragment()

      fragment.arguments = Bundle().apply {
        putInt(
            ERROR_MESSAGE_RESOURCE, errorMessageResource)
        putString(
            FEATURE_ENTRY_BACKSTACK_NAME, entryBackStackName)
        putBoolean(
            SHOW_SUPPORT, showSupport)
      }
      return fragment
    }
  }
}