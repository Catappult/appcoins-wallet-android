package com.asfoundation.wallet.update_required.wallets_list

import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import kotlinx.android.synthetic.main.wallet_selection_item.view.*
import java.util.*


class WalletSelectionAdapter(
  private val context: Context,
  private var items: ArrayList<HashMap<String, String>>,
  @LayoutRes layout: Int,
  from: Array<String>,
  @IdRes to: IntArray,
) : SimpleAdapter(context, items, layout, from, to) {

  override fun getCount(): Int = items.size

  override fun getItem(position: Int): HashMap<String, String> {
    return items[position]
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getView(position: Int, view: View?, parent: ViewGroup): View {
    val views = super.getView(position, view, parent)

    val dateLong = getItem(position)["wallet_backup_date"]!!.toLong()

    views.wallet_selection_name?.text = getItem(position)["wallet_name"]
    views.wallet_selection_balance?.text = getItem(position)["wallet_balance"]

    views.wallet_selection_backup_date?.text = if (dateLong > 0) {
      context.getString(
        R.string.update_backup_date,
        DateFormat.format("dd/MM/yyyy", Date(dateLong))
      )
    } else context.getString(R.string.update_backup_never)

    if (dateLong == 0L) {
      views.wallet_selection_backup_date?.setTextColor(
        ContextCompat.getColor(context, R.color.styleguide_pink)
      )
    }

    return views
  }
}
