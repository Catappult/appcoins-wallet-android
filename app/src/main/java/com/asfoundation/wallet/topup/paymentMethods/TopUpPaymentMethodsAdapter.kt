package com.asfoundation.wallet.topup.paymentMethods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.ui.iab.TopupPaymentMethodsViewHolder
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.Subject


class TopUpPaymentMethodsAdapter(
  private var paymentMethods: List<PaymentMethod>,
  private var paymentMethodClick: PublishRelay<String>,
  private val logoutCallback: () -> Unit,
  private val disposables: CompositeDisposable,
  private val showPayPalLogout: Subject<Boolean>
) :
  RecyclerView.Adapter<TopupPaymentMethodsViewHolder>() {
  private var selectedItem = 0

  fun setSelectedItem(position: Int) {
    selectedItem = position
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupPaymentMethodsViewHolder {
    return TopupPaymentMethodsViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(R.layout.item_topup_payment_method, parent, false)
    )
  }

  override fun getItemCount() = paymentMethods.size

  override fun onBindViewHolder(holder: TopupPaymentMethodsViewHolder, position: Int) {
    holder.bind(
      data = paymentMethods[position],
      checked = selectedItem == position,
      listener = {
        selectedItem = position
        paymentMethodClick.accept(paymentMethods[position].id)
        notifyDataSetChanged()
      },
      onClickListenerTopup = { },
      onClickPaypalLogout = logoutCallback,
      disposables = disposables,
      showPayPalLogout = showPayPalLogout
    )
  }

  fun getSelectedItemData(): PaymentMethod = paymentMethods[selectedItem]
}