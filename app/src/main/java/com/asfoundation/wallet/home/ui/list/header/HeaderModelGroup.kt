package com.asfoundation.wallet.home.ui.list.header

import android.animation.LayoutTransition
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.Async
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.home.ui.list.header.notifications.CardNotificationListModel
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils

class HeaderModelGroup(
  txModelAsync: com.appcoins.wallet.ui.arch.Async<TransactionsModel>,
  balanceAsync: com.appcoins.wallet.ui.arch.Async<GlobalBalance>,
  formatter: CurrencyFormatUtils,
  homeClickListener: ((HomeListClick) -> Unit)? = null
) : EpoxyModelGroup(R.layout.item_home_header_group,
    buildModels(txModelAsync, balanceAsync, formatter, homeClickListener)) {

  private val layoutTransition = LayoutTransition()

  override fun onViewAttachedToWindow(holder: ModelGroupHolder) {
    super.onViewAttachedToWindow(holder)
    layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    holder.rootView.layoutTransition = layoutTransition
  }

  companion object {
    fun buildModels(txModelAsync: com.appcoins.wallet.ui.arch.Async<TransactionsModel>,
                    balanceAsync: com.appcoins.wallet.ui.arch.Async<GlobalBalance>,
                    formatter: CurrencyFormatUtils,
                    homeClickListener: ((HomeListClick) -> Unit)?): List<EpoxyModel<*>> {
      val notifications = txModelAsync()?.notifications
      val models = mutableListOf<EpoxyModel<*>>()

      models.add(HomeWalletInfoModel_()
          .id("home_wallet_info")
          .balanceAsync(balanceAsync)
          .clickListener(homeClickListener)
          .formatter(formatter)
      )
      if (notifications != null && notifications.isNotEmpty()) {
        models.add(CardNotificationListModel(notifications, homeClickListener))
      }
      return models
    }
  }

}