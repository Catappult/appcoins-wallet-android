package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LocalTopUpPaymentFragment : DaggerFragment(), LocalTopUpPaymentView {

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor
  private lateinit var activityView: TopUpActivityView
  private lateinit var presenter: LocalTopUpPaymentPresenter

  companion object {

    private const val PAYMENT_ID = "payment_id"
    private const val PAYMENT_ICON = "payment_icon"
    private const val PAYMENT_LABEL = "payment_label"
    private const val PAYMENT_DATA = "data"

    fun newInstance(paymentId: String, icon: String, label: String,
                    data: TopUpPaymentData): LocalTopUpPaymentFragment {
      val fragment = LocalTopUpPaymentFragment()
      Bundle().apply {
        putString(PAYMENT_ID, paymentId)
        putString(PAYMENT_ICON, icon)
        putString(PAYMENT_LABEL, label)
        putSerializable(PAYMENT_DATA, data)
        fragment.arguments = this
      }
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is TopUpActivityView) {
      throw IllegalStateException("Local topup payment fragment must be attached to Topup activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = LocalTopUpPaymentPresenter(this, activityView, localPaymentInteractor,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), data, paymentId)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_payment_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  private val paymentId: String by lazy {
    if (arguments!!.containsKey(PAYMENT_ID)) {
      arguments!!.getString(PAYMENT_ID)!!
    } else {
      throw IllegalArgumentException("payment id data not found")
    }
  }

  private val paymentIcon: String by lazy {
    if (arguments!!.containsKey(PAYMENT_ICON)) {
      arguments!!.getString(PAYMENT_ICON)!!
    } else {
      throw IllegalArgumentException("payment icon data not found")
    }
  }

  private val paymentLabel: String by lazy {
    if (arguments!!.containsKey(PAYMENT_LABEL)) {
      arguments!!.getString(PAYMENT_LABEL)!!
    } else {
      throw IllegalArgumentException("payment label data not found")
    }
  }

  private val data: TopUpPaymentData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA)!! as TopUpPaymentData
    } else {
      throw IllegalArgumentException("topup payment data not found")
    }
  }
}
