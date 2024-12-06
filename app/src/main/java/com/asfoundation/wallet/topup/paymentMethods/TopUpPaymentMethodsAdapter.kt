package com.asfoundation.wallet.topup.paymentMethods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.TopupPaymentMethodsViewHolder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject


class TopUpPaymentMethodsAdapter(
  private var paymentMethods: List<PaymentMethod>,
  private var paymentMethodClick: PublishSubject<PaymentMethod>,
  private val logoutCallback: () -> Unit,
  private val disposables: CompositeDisposable,
  internal var showLogoutAction: Boolean,
  private val cardsList: List<StoredCard>,
  private val onChangeCardCallback: () -> Unit,
) :
  RecyclerView.Adapter<TopupPaymentMethodsViewHolder>() {
  private var selectedItem = 0

  fun setSelectedItem(position: Int) {
    val tempItem = selectedItem
    selectedItem = position
    notifyItemChanged(tempItem)
    notifyItemChanged(selectedItem)
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
        setSelectedItem(holder.absoluteAdapterPosition)
        paymentMethodClick.onNext(paymentMethods[position])
      },
      onClickLogoutAction = logoutCallback,
      disposables = disposables,
      showLogoutAction = showLogoutAction,
      cardData = cardsList.find { it.isSelectedCard } ?: cardsList.firstOrNull(),
      onChangeCardCallback = onChangeCardCallback
    )
  }

  fun getSelectedItemData(): PaymentMethod = paymentMethods[selectedItem]
}