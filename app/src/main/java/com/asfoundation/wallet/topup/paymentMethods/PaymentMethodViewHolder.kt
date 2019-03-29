package com.asfoundation.wallet.topup.paymentMethods

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.top_up_payment_method_item.view.*

class PaymentMethodViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(data: PaymentMethodData, checked: Boolean, listener: View.OnClickListener) {
    Picasso.with(itemView.context)
        .load(data.imageSrc)
        .into(itemView.payment_method_ic)
    itemView.payment_method_description.text = data.description
    itemView.radio_button.isChecked = checked
    itemView.radio_button.setOnClickListener(listener)
  }
}

data class PaymentMethodData(val imageSrc: String, val description: String, val id: String) :
    Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readString())

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(imageSrc)
    parcel.writeString(description)
    parcel.writeString(id)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<PaymentMethodData> {
    override fun createFromParcel(parcel: Parcel): PaymentMethodData {
      return PaymentMethodData(parcel)
    }

    override fun newArray(size: Int): Array<PaymentMethodData?> {
      return arrayOfNulls(size)
    }
  }
}
