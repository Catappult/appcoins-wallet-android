package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PaymentMethodsFragment : DaggerFragment(), PaymentMethodsView {
  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  lateinit var presenter: PaymentMethodsPresenter
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.payment_methods_layout, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        PaymentMethodsPresenter(this, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable(),
            inAppPurchaseInteractor)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun showPaymentMethods(paymentMethods: List<PaymentMethod>) {
    Log.d(TAG, "showPaymentMethods() called with: paymentMethods = [$paymentMethods]")
  }

  override fun showError() {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  companion object {
    private val TAG = PaymentMethodsFragment::class.java.simpleName
    @JvmStatic
    fun newInstance(): PaymentMethodsFragment {
      val args = Bundle()
      val fragment = PaymentMethodsFragment()
      fragment.arguments = args
      return fragment
    }
  }
}
