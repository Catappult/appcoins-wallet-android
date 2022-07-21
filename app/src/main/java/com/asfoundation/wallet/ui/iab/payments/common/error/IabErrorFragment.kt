package com.asfoundation.wallet.ui.iab.payments.common.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_buy_buttons.*
import kotlinx.android.synthetic.main.fragment_iab_error.*
import javax.inject.Inject

@AndroidEntryPoint
class IabErrorFragment : BasePageViewFragment(), IabErrorView {

  @Inject
  lateinit var presenter: IabErrorPresenter

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
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
    buy_button.setText(getString(R.string.back_button))
    cancel_button.setText(getString(R.string.button_cancel))
  }

  override fun cancelClickEvent(): Observable<Any> = RxView.clicks(cancel_button)

  override fun backClickEvent(): Observable<Any> = RxView.clicks(buy_button)

  override fun getSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun setErrorMessage(errorMessage: String) {
    error_message.text = errorMessage
  }

  companion object {

    internal const val ERROR_MESSAGE_STRING = "error_message_string"
    internal const val ERROR_MESSAGE_RESOURCE = "error_message_resource"
    internal const val FEATURE_ENTRY_BACKSTACK_NAME = "backstack_name"

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
     */
    @JvmStatic
    fun newInstance(errorMessage: String, entryBackStackName: String): IabErrorFragment {
      val fragment = IabErrorFragment()

      fragment.arguments = Bundle().apply {
        putString(ERROR_MESSAGE_STRING, errorMessage)
        putString(FEATURE_ENTRY_BACKSTACK_NAME, entryBackStackName)
      }
      return fragment
    }

    @JvmStatic
    fun newInstance(
      @StringRes errorMessageResource: Int,
      entryBackStackName: String
    ): IabErrorFragment {
      val fragment = IabErrorFragment()

      fragment.arguments = Bundle().apply {
        putInt(ERROR_MESSAGE_RESOURCE, errorMessageResource)
        putString(FEATURE_ENTRY_BACKSTACK_NAME, entryBackStackName)
      }
      return fragment
    }
  }
}