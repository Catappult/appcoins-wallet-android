package com.asfoundation.wallet.ui.iab.payments.carrier.status

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_carrier_payment_status.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.view.*
import javax.inject.Inject

class CarrierPaymentFragment : DaggerFragment(), CarrierPaymentView {

  @Inject
  lateinit var presenter: CarrierPaymentPresenter
  lateinit var iabView: IabView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_carrier_payment_status, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  private fun setupUi() {
    iabView.disableBack()
    lockRotation()
  }

  override fun onDestroyView() {
    iabView.enableBack()
    unlockRotation()
    presenter.stop()
    super.onDestroyView()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "CarrierPaymentStatusFragment must be attached to IAB activity" }
    iabView = context
  }

  private fun unlockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  private fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun setLoading() {
    progress_bar.visibility = View.VISIBLE
    complete_payment_view.visibility = View.INVISIBLE
  }

  override fun showFinishedTransaction() {
    complete_payment_view.visibility = View.VISIBLE
    progress_bar.visibility = View.INVISIBLE
  }

  override fun getFinishedDuration(): Long =
      complete_payment_view.lottie_transaction_success.duration

  companion object {

    internal const val DOMAIN_KEY = "domain"
    internal const val TRANSACTION_DATA_KEY = "transaction_data"
    internal const val TRANSACTION_TYPE_KEY = "transaction_type"
    internal const val PAYMENT_URL = "payment_url"

    @JvmStatic
    fun newInstance(domain: String, transactionData: String,
                    transactionType: String, paymentUrl: String): CarrierPaymentFragment {
      val fragment =
          CarrierPaymentFragment()
      fragment.arguments = Bundle().apply {
        putString(
            DOMAIN_KEY, domain)
        putString(
            TRANSACTION_DATA_KEY, transactionData)
        putString(
            TRANSACTION_TYPE_KEY, transactionType)
        putString(
            PAYMENT_URL, paymentUrl)
      }
      return fragment
    }
  }
}