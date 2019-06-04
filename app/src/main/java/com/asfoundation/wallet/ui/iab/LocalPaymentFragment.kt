package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.router.ExternalBrowserRouter
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class LocalPaymentFragment : DaggerFragment(), LocalPaymentView {
  companion object {

    private const val DOMAIN_KEY = "domain"
    private const val SKU_ID_KEY = "skuId"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val PAYMENT_KEY = "payment_name"

    @JvmStatic
    fun newInstance(domain: String, skudId: String, amount: String?, currency: String?,
                    selectedPaymentMethod: String): LocalPaymentFragment {
      val fragment = LocalPaymentFragment()
      val bundle = Bundle()
      bundle.putString(DOMAIN_KEY, domain)
      bundle.putString(SKU_ID_KEY, skudId)
      bundle.putString(AMOUNT_KEY, amount)
      bundle.putString(CURRENCY_KEY, currency)
      bundle.putString(PAYMENT_KEY, selectedPaymentMethod)
      fragment.arguments = bundle
      return fragment
    }
  }

  val domain: String by lazy {
    if (arguments!!.containsKey(DOMAIN_KEY)) {
      arguments!!.getString(DOMAIN_KEY)
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }
  val skudId: String by lazy {
    if (arguments!!.containsKey(SKU_ID_KEY)) {
      arguments!!.getString(SKU_ID_KEY)
    } else {
      throw IllegalArgumentException("skuId data not found")
    }
  }
  val amount: String? by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getString(AMOUNT_KEY)
    } else {
      throw IllegalArgumentException("amount data not found")
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

  private lateinit var iabView: IabView
  private lateinit var navigator: FragmentNavigator
  private lateinit var browserRouter: ExternalBrowserRouter
  private lateinit var localPaymentPresenter: LocalPaymentPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    navigator = FragmentNavigator(activity as UriNavigator?, iabView)
    browserRouter = ExternalBrowserRouter()
    localPaymentPresenter =
        LocalPaymentPresenter(this, amount, currency, domain, skudId,
            paymentId, localPaymentInteractor, navigator, CompositeDisposable())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is IabView) {
      throw IllegalStateException("Regular buy fragment must be attached to IAB activity")
    }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    localPaymentPresenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.local_payment_layout, container, false)
  }

}
