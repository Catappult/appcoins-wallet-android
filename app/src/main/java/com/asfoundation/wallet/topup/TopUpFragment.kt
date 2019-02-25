package com.asfoundation.wallet.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodAdapter
import com.jakewharton.rxbinding2.InitialValueObservable
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_top_up.*
import javax.inject.Inject

class TopUpFragment : DaggerFragment(), TopUpFragmentView {

  @Inject
  lateinit var interactor: TopUpInteractor
  private lateinit var adapter: TopUpPaymentMethodAdapter
  private lateinit var presenter: TopUpFragmentPresenter

  companion object {
    @JvmStatic
    fun newInstance(): TopUpFragment {
      return TopUpFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        TopUpFragmentPresenter(this, interactor, AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUiElements(data: UiData) {
    changeCurrency(data.currency)
    main_value.isEnabled = true

    adapter = TopUpPaymentMethodAdapter(data.methods)
    payment_method.adapter = adapter
    payment_method.layoutManager = LinearLayoutManager(context)

    swap_value_button.isEnabled = true
  }

  override fun getChangeCurrencyClick(): Observable<Any> {
    return RxView.clicks(swap_value_button)
  }

  override fun getEditTextChanges(): InitialValueObservable<TextViewAfterTextChangeEvent> {
    return RxTextView.afterTextChangeEvents(main_value)
  }

  override fun changeCurrency(data: CurrencyData) {
    currency_code.text = data.fiatCurrencyCode
    if (main_value.text.toString() != data.fiatValue) {
      main_value.setText(data.fiatValue)
    }
    main_value_currency.text = data.fiatCurrencySymbol

    swap_value_lable.text = data.appcCode
    converted_value.text = "${data.appcValue} ${data.appcSymbol}"
  }
}
