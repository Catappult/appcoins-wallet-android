package com.asfoundation.wallet.home.ui.list.header.notifications

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction

class AppcoinsAppListModel(val data: List<AppcoinsApplication>,
                           val listener: ((AppcoinsApplication, ApplicationClickAction) -> Unit)?) :
    EpoxyModelGroup(R.layout.item_appcoins_application_list, buildModels(data, listener)) {

  companion object {
    fun buildModels(data: List<AppcoinsApplication>,
                    listener: ((AppcoinsApplication, ApplicationClickAction) -> Unit)?): List<EpoxyModel<*>> {
      val appModels = mutableListOf<AppCoinsAppModel_>()
      for (app in data) {
        appModels.add(
            AppCoinsAppModel_()
                .id(app.packageName + app.uniqueName)
                .appCoinsApplication(app)
                .clickListener(listener)
        )
      }
      return listOf(
          DefaultCarouselModel_()
              .numViewsToShowOnScreen(1.26f)
              .id("appcoins_app_list")
              .models(appModels)
      )
    }
  }
}