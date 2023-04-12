package com.asfoundation.wallet.ui.iab.payments.common.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentIabErrorBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class IabErrorFragment : BasePageViewFragment(), IabErrorView {

  @Inject
  lateinit var presenter: IabErrorPresenter

  private val binding by viewBinding(FragmentIabErrorBinding::bind)

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
    binding.dialogBuyButtons.buyButton.setText(getString(R.string.back_button))
    binding.dialogBuyButtons.cancelButton.setText(getString(R.string.button_cancel))
  }

  override fun cancelClickEvent(): Observable<Any> = RxView.clicks(binding.dialogBuyButtons.cancelButton)

  override fun backClickEvent(): Observable<Any> = RxView.clicks(binding.dialogBuyButtons.buyButton)

  override fun getSupportLogoClicks() = RxView.clicks(binding.layoutSupportLogo)

  override fun getSupportIconClicks() = RxView.clicks(binding.layoutSupportIcn)

  override fun setErrorMessage(errorMessage: String) {
    binding.errorMessage.text = errorMessage
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