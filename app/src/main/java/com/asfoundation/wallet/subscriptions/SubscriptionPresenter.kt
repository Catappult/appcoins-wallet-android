package com.asfoundation.wallet.subscriptions

class SubscriptionPresenter(private val activity: SubscriptionView) {


  fun present(actionMode: Int, appPackage: String?) {
    when (actionMode) {
      SubscriptionActivity.ACTION_LIST -> activity.showSubscriptionList()
      SubscriptionActivity.ACTION_DETAILS -> activity.showSubscriptionDetails(appPackage!!)
      SubscriptionActivity.ACTION_CANCEL -> activity.showCancelSubscription(appPackage!!)
    }
  }

}