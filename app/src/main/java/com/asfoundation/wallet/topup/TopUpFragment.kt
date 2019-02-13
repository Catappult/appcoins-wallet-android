package com.asfoundation.wallet.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.asf.wallet.R
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.topup.paymentMethods.TopUpPaymentMethodAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_top_up.*

class TopUpFragment : DaggerFragment() {
  private lateinit var adapter: TopUpPaymentMethodAdapter

  companion object {
    @JvmStatic
    fun newInstance(): TopUpFragment {
      return TopUpFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    adapter = TopUpPaymentMethodAdapter(
        mutableListOf(PaymentMethodData("paypal", "paypal"),
            PaymentMethodData("paypal", "paypal")))

  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    payment_method.adapter = adapter
    payment_method.layoutManager = LinearLayoutManager(context)

  }

}
