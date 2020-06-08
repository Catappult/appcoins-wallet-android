package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_adyen_top_up.*
import javax.inject.Inject

class LocalTopUpPaymentFragment : DaggerFragment(), LocalTopUpPaymentView {

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor

  @Inject
  lateinit var formatter: CurrencyFormatUtils
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
    presenter = LocalTopUpPaymentPresenter(this, activityView, localPaymentInteractor, formatter,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(), data, paymentId)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_topup_payment_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun showValues(value: String, currency: String, appcValue: String) {
    main_value.visibility = View.VISIBLE
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      main_value.setText(value)
      main_currency_code.text = currency
      converted_value.text = "$appcValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      main_value.setText(appcValue)
      main_currency_code.text = WalletCurrency.CREDITS.symbol
      converted_value.text = "$value $currency"
    }
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
