package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.local_payment_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class LocalPaymentFragment : DaggerFragment(), LocalPaymentView {

  companion object {

    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val URI_KEY = "uri_key"
    private const val PAYMENT_KEY = "payment_name"
    private const val IS_BDS = "is_bds"
    private const val TRANSACTION_KEY = "transaction_key"

    @JvmStatic
    fun newInstance(amount: BigDecimal, currency: String?,
                    transaction: TransactionBuilder,
                    data: String, isBds: Boolean,
                    selectedPaymentMethod: String): LocalPaymentFragment {
      val fragment = LocalPaymentFragment()
      val bundle = Bundle()
      bundle.putString(AMOUNT_KEY, amount.toString())
      bundle.putString(CURRENCY_KEY, currency)
      bundle.putParcelable(TRANSACTION_KEY, transaction)
      bundle.putString(URI_KEY, data)
      bundle.putBoolean(IS_BDS, isBds)
      bundle.putString(PAYMENT_KEY, selectedPaymentMethod)
      fragment.arguments = bundle
      return fragment
    }
  }

  val amount: String by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getString(AMOUNT_KEY)
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  val uri: String by lazy {
    if (arguments!!.containsKey(URI_KEY)) {
      arguments!!.getString(URI_KEY)
    } else {
      throw IllegalArgumentException("uri data not found")
    }
  }

  val paymentId: String by lazy {
    if (arguments!!.containsKey(PAYMENT_KEY)) {
      arguments!!.getString(PAYMENT_KEY)
    } else {
      throw IllegalArgumentException("payment method data not found")
    }
  }

  val currency: String? by lazy {
    if (arguments!!.containsKey(CURRENCY_KEY)) {
      arguments!!.getString(CURRENCY_KEY)
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  @Inject
  lateinit var localPaymentInteractor: LocalPaymentInteractor
  private lateinit var localPaymentPresenter: LocalPaymentPresenter
  private var transaction: TransactionBuilder? = null
  private var isBds: Boolean = true


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    transaction = arguments?.let { it.getParcelable(TRANSACTION_KEY) }
    isBds = arguments?.let { it.getBoolean(IS_BDS) }!!
    localPaymentPresenter =
        LocalPaymentPresenter(this, amount, currency, transaction!!.domain, transaction!!.skuId,
            uri, isBds, paymentId, localPaymentInteractor, CompositeDisposable())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    localPaymentPresenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_payment_layout, container, false)
  }

  override fun showLink(link: String) {
    initial_test_value.text = link
  }
}